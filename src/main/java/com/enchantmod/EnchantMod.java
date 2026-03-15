package com.enchantmod;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Mod(EnchantMod.MOD_ID)
public class EnchantMod {

    public static final String MOD_ID = "enchantmod";
    public static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private static final Set<UUID> explodedArrows = new HashSet<>();
    private static final UUID BLADE_FURY_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    public EnchantMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEnchantments.ENCHANTMENTS.register(modBus);
        ModEffects.MOB_EFFECTS.register(modBus);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("[EnchantMod] Loaded!");
    }

    private boolean isBow(ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    private boolean isSword(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof SwordItem) return true;
        if (stack.is(ItemTags.SWORDS)) return true;
        return false;
    }

    private boolean hasInfernum(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.INFERNUM.get(), stack) > 0;
    }

    // Returns duration in ticks for BLUE_HELLFIRE based on Fire Aspect / Flame level.
    // Returns 0 if neither enchantment is present.
    private int getInfernumDuration(ItemStack weapon, boolean isBowWeapon) {
        if (isBowWeapon) {
            int flame = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, weapon);
            if (flame > 0) return 100; // 5 seconds
            return 0;
        } else {
            int fireAspect = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, weapon);
            if (fireAspect >= 2) return 160; // 8 seconds
            if (fireAspect == 1) return 80;  // 4 seconds
            return 0;
        }
    }

    // Check if entity is immune to fire (blazes, ghasts, nether mobs, etc.)
    private boolean isFireImmune(LivingEntity entity) {
        return entity.fireImmune();
    }

    // Check if entity is in water or rain
    private boolean isInWaterOrRain(LivingEntity entity) {
        if (entity.isInWater()) return true;
        // Check rain: level is raining and entity is exposed to sky
        if (entity.level().isRaining() && entity.level().canSeeSky(entity.blockPosition())) return true;
        FluidState fluid = entity.level().getFluidState(entity.blockPosition());
        if (fluid.is(Fluids.WATER) || fluid.is(Fluids.FLOWING_WATER)) return true;
        return false;
    }

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!right.is(Items.ENCHANTED_BOOK)) return;
        Map<Enchantment, Integer> bookEnchants = EnchantmentHelper.getEnchantments(right);
        for (Enchantment ench : bookEnchants.keySet()) {
            if (ench == ModEnchantments.BLAST_SHOT.get()) {
                if (!isBow(left)) { event.setCanceled(true); return; }
                Map<Enchantment, Integer> leftEnchants = EnchantmentHelper.getEnchantments(left);
                for (Enchantment existing : leftEnchants.keySet()) {
                    if (!ench.isCompatibleWith(existing)) { event.setCanceled(true); return; }
                }
            }
            if (ench == ModEnchantments.VAMPIRISM.get()) {
                if (!isSword(left)) { event.setCanceled(true); return; }
            }
            if (ench == ModEnchantments.BLADE_FURY.get()) {
                if (!isSword(left)) { event.setCanceled(true); return; }
            }
            if (ench == ModEnchantments.INFERNUM.get()) {
                if (!isSword(left) && !isBow(left)) { event.setCanceled(true); return; }
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();

        // Block ALL fire damage while blue hellfire is active
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            if (target.hasEffect(ModEffects.BLUE_HELLFIRE.get())) {
                event.setCanceled(true);
                return;
            }
        }

        if (!(event.getSource().getEntity() instanceof Player player)) return;
        ItemStack weapon = player.getMainHandItem();

        // Block vanilla fire from Fire Aspect / Flame when Infernum is present
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            if (hasInfernum(weapon)) {
                event.setCanceled(true);
                return;
            }
            ItemStack offBow = player.getOffhandItem();
            if ((isBow(weapon) && hasInfernum(weapon)) ||
                (isBow(offBow) && hasInfernum(offBow))) {
                event.setCanceled(true);
                return;
            }
        }

        // Vampirism
        if (isSword(weapon)) {
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.VAMPIRISM.get(), weapon);
            if (level > 0) {
                float[] chances = {0.0f, 0.10f, 0.15f, 0.20f};
                float chance = level < chances.length ? chances[level] : 0.20f;
                if (RANDOM.nextFloat() < chance) {
                    float heal = event.getAmount() * 0.50f;
                    if (heal > 0) {
                        player.heal(heal);
                        player.level().playSound(null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.PLAYERS, 0.5f, 1.8f);
                    }
                }
            }
        }

        // Infernum - sword hit
        if (isSword(weapon) && hasInfernum(weapon)) {
            // Skip fire-immune mobs
            if (isFireImmune(target)) return;
            int duration = getInfernumDuration(weapon, false);
            if (duration <= 0) return;
            target.addEffect(new MobEffectInstance(
                ModEffects.BLUE_HELLFIRE.get(), duration, 0, false, false
            ));
        }
    }

    // Every tick: water/rain extinguishes effect, fire kept clear, particles, sound
    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect(ModEffects.BLUE_HELLFIRE.get())) return;

        // Water or rain extinguishes the effect
        if (isInWaterOrRain(entity)) {
            entity.removeEffect(ModEffects.BLUE_HELLFIRE.get());
            return;
        }

        // Keep vanilla fire gone every single tick
        if (entity.getRemainingFireTicks() > 0) {
            entity.setRemainingFireTicks(-1);
        }

        // Wither ambient sound every 50 ticks, high pitch, quiet
        if (entity.tickCount % 50 == 0) {
            entity.level().playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.WITHER_AMBIENT,
                SoundSource.HOSTILE,
                0.12f, 1.4f
            );
        }

        // Spawn particles server-side
        if (!entity.level().isClientSide() && entity.level() instanceof ServerLevel serverLevel) {
            double x = entity.getX();
            double y = entity.getY();
            double z = entity.getZ();
            float w = entity.getBbWidth();
            float h = entity.getBbHeight();

            // Soul fire flame - 2 per tick, random positions around body
            for (int i = 0; i < 2; i++) {
                double ox = (RANDOM.nextDouble() - 0.5) * w * 1.4;
                double oy = 0.3 + RANDOM.nextDouble() * h * 0.8;
                double oz = (RANDOM.nextDouble() - 0.5) * w * 1.4;
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    x + ox, y + oy, z + oz,
                    1, 0.0, 0.05, 0.0, 0.01
                );
            }

            // Soul particles - orbit around mob, 1 every 4 ticks
            if (entity.tickCount % 4 == 0) {
                double angle = (entity.tickCount * 18.0) * Math.PI / 180.0;
                double radius = w * 1.1 + 0.3;
                double ox = Math.cos(angle) * radius;
                double oz = Math.sin(angle) * radius;
                double oy = 0.5 + h * 0.4 + Math.sin(angle * 0.7) * 0.3;
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.SOUL,
                    x + ox, y + oy, z + oz,
                    1, 0.0, 0.02, 0.0, 0.0
                );
            }
        }
    }

    // On death while BLUE_HELLFIRE is active: drop cooked loot (same as vanilla fire death)
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect(ModEffects.BLUE_HELLFIRE.get())) return;
        // Set fire ticks so vanilla loot table sees "died by fire" and gives cooked drops
        // We use a very high value so it won't be cleared before loot is rolled
        if (entity.getRemainingFireTicks() <= 0) {
            entity.setSecondsOnFire(100);
        }
    }

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getOwner() instanceof Player player)) return;
        if (arrow.level().isClientSide()) return;
        if (!(arrow.level() instanceof ServerLevel serverLevel)) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ItemStack bow = isBow(mainHand) ? mainHand : isBow(offHand) ? offHand : null;

        // Infernum - bow hit
        if (bow != null && hasInfernum(bow)) {
            if (event.getRayTraceResult() instanceof EntityHitResult entityHit) {
                if (entityHit.getEntity() instanceof LivingEntity hitTarget) {
                    // Skip fire-immune mobs
                    if (!isFireImmune(hitTarget)) {
                        int duration = getInfernumDuration(bow, true);
                        if (duration > 0) {
                            hitTarget.addEffect(new MobEffectInstance(
                                ModEffects.BLUE_HELLFIRE.get(), duration, 0, false, false
                            ));
                        }
                    }
                }
            }
        }

        // Blast Shot
        UUID arrowId = arrow.getUUID();
        if (explodedArrows.contains(arrowId)) return;

        int level = 0;
        if (bow != null) {
            level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BLAST_SHOT.get(), bow);
        }
        if (level <= 0) return;

        float[] chances = {0.0f, 0.30f, 0.40f};
        float chance = level < chances.length ? chances[level] : 0.40f;
        if (RANDOM.nextFloat() >= chance) return;

        explodedArrows.add(arrowId);
        if (explodedArrows.size() > 100) explodedArrows.clear();

        Vec3 pos = arrow.position();
        float[] damages = {0.0f, 5.0f, 7.0f};
        float dmg = level < damages.length ? damages[level] : 7.0f;

        LivingEntity hitTarget = null;
        if (event.getRayTraceResult() instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof LivingEntity le) hitTarget = le;
        }
        final LivingEntity finalHitTarget = hitTarget;

        DamageSource blastDamage = serverLevel.damageSources().magic();
        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
            LivingEntity.class,
            new AABB(pos.x-2.5, pos.y-2.5, pos.z-2.5, pos.x+2.5, pos.y+2.5, pos.z+2.5)
        );
        for (LivingEntity mob : nearby) {
            if (mob == player) continue;
            if (mob == finalHitTarget) {
                Vec3 dir = mob.position().subtract(pos).normalize();
                mob.setDeltaMovement(mob.getDeltaMovement().add(dir.x*1.2, 0.4, dir.z*1.2));
                mob.hurtMarked = true;
                continue;
            }
            double dist = mob.position().distanceTo(pos);
            if (dist > 2.5) continue;
            mob.hurt(blastDamage, dmg);
            Vec3 dir = mob.position().subtract(pos).normalize();
            mob.setDeltaMovement(mob.getDeltaMovement().add(dir.x*1.2, 0.4, dir.z*1.2));
            mob.hurtMarked = true;
        }
        serverLevel.explode(null, pos.x, pos.y, pos.z, 1.2f, Level.ExplosionInteraction.NONE);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Player player = event.player;
        ItemStack weapon = player.getMainHandItem();

        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) return;
        attackSpeed.removeModifier(BLADE_FURY_UUID);

        if (!isSword(weapon)) return;
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BLADE_FURY.get(), weapon);
        if (level <= 0) return;

        attackSpeed.addTransientModifier(new AttributeModifier(
            BLADE_FURY_UUID, "blade_fury_speed", 1.2, AttributeModifier.Operation.ADDITION
        ));
    }
}

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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
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

        // Block Fire Aspect and Flame if Infernum is on the weapon
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            if (hasInfernum(weapon)) {
                event.setCanceled(true);
                return;
            }
            ItemStack mainBow = player.getMainHandItem();
            ItemStack offBow = player.getOffhandItem();
            if ((isBow(mainBow) && hasInfernum(mainBow)) ||
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

        // Infernum - sword
        if (isSword(weapon) && hasInfernum(weapon)) {
            target.addEffect(new MobEffectInstance(
                ModEffects.BLUE_HELLFIRE.get(), 120, 0, false, false
            ));
        }
    }

    // Every tick: if target has blue hellfire - keep vanilla fire cleared
    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect(ModEffects.BLUE_HELLFIRE.get())) return;
        // Keep vanilla fire gone every single tick
        if (entity.getRemainingFireTicks() > 0) {
            entity.setRemainingFireTicks(-1);
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

        // Infernum - bow
        if (bow != null && hasInfernum(bow)) {
            if (event.getRayTraceResult() instanceof EntityHitResult entityHit) {
                if (entityHit.getEntity() instanceof LivingEntity hitTarget) {
                    hitTarget.addEffect(new MobEffectInstance(
                        ModEffects.BLUE_HELLFIRE.get(), 120, 0, false, false
                    ));
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

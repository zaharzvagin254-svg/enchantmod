package com.enchantmod;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
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

    // UUID для модификатора скорости атаки
    private static final UUID BLADE_FURY_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    public EnchantMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEnchantments.ENCHANTMENTS.register(modBus);
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

    // Наковальня - блокируем неправильные комбинации
    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!right.is(Items.ENCHANTED_BOOK)) return;
        Map<Enchantment, Integer> bookEnchants = EnchantmentHelper.getEnchantments(right);
        for (Enchantment ench : bookEnchants.keySet()) {
            if (ench == ModEnchantments.BLAST_SHOT.get()) {
                if (!isBow(left)) { event.setCanceled(true); return; }
                // Проверяем что на луке нет несовместимых чар
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
        }
    }

    // Vampirism
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        ItemStack weapon = player.getMainHandItem();
        if (!isSword(weapon)) return;
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.VAMPIRISM.get(), weapon);
        if (level <= 0) return;
        float[] chances = {0.0f, 0.10f, 0.15f, 0.20f};
        float chance = level < chances.length ? chances[level] : 0.20f;
        if (RANDOM.nextFloat() < chance) {
            float heal = event.getAmount() * 0.50f;
            if (heal > 0) {
                player.heal(heal);
                player.level().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.5f, 1.8f
                );
            }
        }
    }

    // Blade Fury - ускорение перезарядки меча через атрибут ATTACK_SPEED
    // Level 1: +0.5x скорости (1.5x), Level 2: +1.0x скорости (2x)
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Player player = event.player;
        ItemStack weapon = player.getMainHandItem();

        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) return;

        // Убираем старый модификатор
        attackSpeed.removeModifier(BLADE_FURY_UUID);

        if (!isSword(weapon)) return;
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BLADE_FURY.get(), weapon);
        if (level <= 0) return;

        // Level 1: +0.5 (базовая скорость 4.0, станет ~6.0 = 1.5x)
        // Level 2: +4.0 (станет ~8.0 = 2x)
        double[] speedBonus = {0.0, 2.0, 4.0};
        double bonus = level < speedBonus.length ? speedBonus[level] : 4.0;

        attackSpeed.addTransientModifier(new AttributeModifier(
            BLADE_FURY_UUID,
            "blade_fury_speed",
            bonus,
            AttributeModifier.Operation.ADDITION
        ));
    }

    // Blast Shot
    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getOwner() instanceof Player player)) return;
        if (arrow.level().isClientSide()) return;
        if (!(arrow.level() instanceof ServerLevel serverLevel)) return;

        UUID arrowId = arrow.getUUID();
        if (explodedArrows.contains(arrowId)) return;

        int level = 0;
        ItemStack mainHand = player.getMainHandItem();
        if (isBow(mainHand)) {
            level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BLAST_SHOT.get(), mainHand);
        }
        if (level <= 0) {
            ItemStack offHand = player.getOffhandItem();
            if (isBow(offHand)) {
                level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BLAST_SHOT.get(), offHand);
            }
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
            if (entityHit.getEntity() instanceof LivingEntity le) {
                hitTarget = le;
            }
        }
        final LivingEntity finalHitTarget = hitTarget;

        DamageSource blastDamage = serverLevel.damageSources().magic();
        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
            LivingEntity.class,
            new AABB(pos.x - 2.5, pos.y - 2.5, pos.z - 2.5, pos.x + 2.5, pos.y + 2.5, pos.z + 2.5)
        );
        for (LivingEntity mob : nearby) {
            if (mob == player) continue;
            if (mob == finalHitTarget) {
                Vec3 dir = mob.position().subtract(pos).normalize();
                mob.setDeltaMovement(mob.getDeltaMovement().add(dir.x * 1.2, 0.4, dir.z * 1.2));
                mob.hurtMarked = true;
                continue;
            }
            double dist = mob.position().distanceTo(pos);
            if (dist > 2.5) continue;
            mob.hurt(blastDamage, dmg);
            Vec3 dir = mob.position().subtract(pos).normalize();
            mob.setDeltaMovement(mob.getDeltaMovement().add(dir.x * 1.2, 0.4, dir.z * 1.2));
            mob.hurtMarked = true;
        }

        serverLevel.explode(null, pos.x, pos.y, pos.z, 1.2f, Level.ExplosionInteraction.NONE);
    }
}

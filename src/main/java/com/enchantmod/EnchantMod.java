package com.enchantmod;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

@Mod(EnchantMod.MOD_ID)
public class EnchantMod {

    public static final String MOD_ID = "enchantmod";
    public static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();

    public EnchantMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEnchantments.ENCHANTMENTS.register(modBus);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("[EnchantMod] Loaded!");
    }

    // ===================== КРОВАВОЕ ПОГЛОЩЕНИЕ =====================
    // Шанс 10%/20%/30% восстановить 50% нанесённого урона
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        ItemStack weapon = player.getMainHandItem();
        int level = EnchantmentHelper.getItemEnchantmentLevel(
            ModEnchantments.BLOOD_LEECH.get(), weapon
        );
        if (level <= 0) return;

        float[] chances = {0.0f, 0.10f, 0.20f, 0.30f};
        float chance = level < chances.length ? chances[level] : 0.30f;

        if (RANDOM.nextFloat() < chance) {
            float heal = event.getAmount() * 0.50f;
            if (heal > 0) player.heal(heal);
        }
    }

    // ===================== ВЗРЫВНОЙ ВЫСТРЕЛ =====================
    // Взрыв при попадании стрелы в любой объект (моб или блок)
    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getOwner() instanceof Player player)) return;
        if (arrow.level().isClientSide()) return;
        if (!(arrow.level() instanceof ServerLevel serverLevel)) return;

        ItemStack bow = findEnchantedBow(player);
        int level = EnchantmentHelper.getItemEnchantmentLevel(
            ModEnchantments.BLAST_SHOT.get(), bow
        );
        if (level <= 0) return;

        float[] chances = {0.0f, 0.20f, 0.35f, 0.50f};
        float chance = level < chances.length ? chances[level] : 0.50f;
        if (RANDOM.nextFloat() >= chance) return;

        Vec3 pos = arrow.position();

        // Визуальный взрыв как TNT — звук + частицы, блоки НЕ ломает
        serverLevel.explode(
            null,
            pos.x, pos.y, pos.z,
            2.5f,
            Level.ExplosionInteraction.NONE
        );

        // Урон и отталкивание мобов в радиусе 3.5 блока
        DamageSource blastDamage = serverLevel.damageSources().explosion(arrow, player);
        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
            LivingEntity.class,
            new AABB(pos.x - 3.5, pos.y - 3.5, pos.z - 3.5,
                     pos.x + 3.5, pos.y + 3.5, pos.z + 3.5)
        );

        for (LivingEntity mob : nearby) {
            if (mob == player) continue;
            double dist = mob.position().distanceTo(pos);
            if (dist > 3.5) continue;

            // Урон от 8 до 2 в зависимости от расстояния
            float dmg = (float)(8.0 * (1.0 - dist / 3.5));
            mob.hurt(blastDamage, Math.max(dmg, 2.0f));

            // Отталкивание
            Vec3 dir = mob.position().subtract(pos).normalize();
            mob.setDeltaMovement(mob.getDeltaMovement().add(
                dir.x * 1.8, 0.6, dir.z * 1.8
            ));
            mob.hurtMarked = true;
        }
    }

    private ItemStack findEnchantedBow(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if ((stack.is(Items.BOW) || stack.is(Items.CROSSBOW))
                && EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.BLAST_SHOT.get(), stack) > 0) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}

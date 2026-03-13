package com.enchantmod;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
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

    public EnchantMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEnchantments.ENCHANTMENTS.register(modBus);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("[EnchantMod] Loaded!");
    }

    // Anvil check - block wrong enchantment combinations
    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!right.is(Items.ENCHANTED_BOOK)) return;

        Map<Enchantment, Integer> bookEnchants = EnchantmentHelper.getEnchantments(right);
        for (Enchantment ench : bookEnchants.keySet()) {
            if (ench == ModEnchantments.BLAST_SHOT.get()) {
                if (!(left.getItem() instanceof BowItem) && !(left.getItem() instanceof CrossbowItem)) {
                    event.setCanceled(true);
                    return;
                }
            }
            if (ench == ModEnchantments.BLOOD_LEECH.get()) {
                if (!(left.getItem() instanceof SwordItem)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    // Blood Leech
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof SwordItem)) return;
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BLOOD_LEECH.get(), weapon);
        if (level <= 0) return;
        float[] chances = {0.0f, 0.10f, 0.15f, 0.20f};
        float chance = level < chances.length ? chances[level] : 0.20f;
        if (RANDOM.nextFloat() < chance) {
            float heal = event.getAmount() * 0.50f;
            if (heal > 0) player.heal(heal);
        }
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

        ItemStack mainHand = player.getMainHandItem();
        int level = 0;
        if (mainHand.is(Items.BOW) || mainHand.is(Items.CROSSBOW)) {
            level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BLAST_SHOT.get(), mainHand);
        }
        if (level <= 0) {
            ItemStack offHand = player.getOffhandItem();
            if (offHand.is(Items.BOW) || offHand.is(Items.CROSSBOW)) {
                level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BLAST_SHOT.get(), offHand);
            }
        }
        if (level <= 0) return;

        float[] chances = {0.0f, 0.20f, 0.35f, 0.50f};
        float chance = level < chances.length ? chances[level] : 0.50f;
        if (RANDOM.nextFloat() >= chance) return;

        explodedArrows.add(arrowId);
        if (explodedArrows.size() > 100) explodedArrows.clear();

        Vec3 pos = arrow.position();
        serverLevel.explode(null, pos.x, pos.y, pos.z, 1.2f, Level.ExplosionInteraction.NONE);

        DamageSource blastDamage = serverLevel.damageSources().explosion(arrow, player);
        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
            LivingEntity.class,
            new AABB(pos.x - 2.5, pos.y - 2.5, pos.z - 2.5, pos.x + 2.5, pos.y + 2.5, pos.z + 2.5)
        );
        for (LivingEntity mob : nearby) {
            if (mob == player) continue;
            double dist = mob.position().distanceTo(pos);
            if (dist > 2.5) continue;
            float dmg = (float)(6.0 * (1.0 - dist / 2.5));
            mob.hurt(blastDamage, Math.max(dmg, 2.0f));
            Vec3 dir = mob.position().subtract(pos).normalize();
            mob.setDeltaMovement(mob.getDeltaMovement().add(dir.x * 1.2, 0.4, dir.z * 1.2));
            mob.hurtMarked = true;
        }
    }
}

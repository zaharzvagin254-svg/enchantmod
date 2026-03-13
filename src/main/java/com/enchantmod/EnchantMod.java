package com.enchantmod;

import com.enchantmod.enchantments.BlastShotEnchantment;
import com.enchantmod.enchantments.BloodLeechEnchantment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Explosion;
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
        LOGGER.info("[EnchantMod] Loaded! Blast Shot and Blood Leech enchantments added.");
    }

    // Blood Leech - heal on hit
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack weapon = player.getMainHandItem();
        int level = EnchantmentHelper.getItemEnchantmentLevel(
            ModEnchantments.BLOOD_LEECH.get(), weapon
        );
        if (level <= 0) return;

        float damage = event.getAmount();
        float healPercent = level * 0.05f; // 5% per level
        float healAmount = damage * healPercent;

        if (healAmount > 0) {
            player.heal(healAmount);
        }
    }

    // Blast Shot - explode on arrow impact
    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getOwner() instanceof Player player)) return;
        if (arrow.level().isClientSide()) return;

        ItemStack bow = findBowOrCrossbow(player);
        int level = EnchantmentHelper.getItemEnchantmentLevel(
            ModEnchantments.BLAST_SHOT.get(), bow
        );
        if (level <= 0) return;

        float[] chances = {0.0f, 0.20f, 0.35f, 0.50f};
        float chance = level < chances.length ? chances[level] : 0.50f;

        if (RANDOM.nextFloat() < chance) {
            Vec3 pos = arrow.position();
            // Create explosion that does NOT break blocks
            arrow.level().explode(
                null,
                pos.x, pos.y, pos.z,
                2.5f,
                Explosion.BlockInteraction.KEEP
            );
            // Extra knockback to nearby mobs
            List<LivingEntity> nearby = arrow.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(pos.x - 3, pos.y - 3, pos.z - 3,
                         pos.x + 3, pos.y + 3, pos.z + 3)
            );
            for (LivingEntity mob : nearby) {
                if (mob == player) continue;
                Vec3 dir = mob.position().subtract(pos).normalize();
                mob.setDeltaMovement(mob.getDeltaMovement().add(dir.x * 1.5, 0.5, dir.z * 1.5));
                mob.hurtMarked = true;
            }
        }
    }

    private ItemStack findBowOrCrossbow(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.is(Items.BOW) || main.is(Items.CROSSBOW)) return main;
        ItemStack off = player.getOffhandItem();
        if (off.is(Items.BOW) || off.is(Items.CROSSBOW)) return off;
        // Check inventory for recently used bow
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.BOW) || stack.is(Items.CROSSBOW)) {
                int level = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.BLAST_SHOT.get(), stack
                );
                if (level > 0) return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}

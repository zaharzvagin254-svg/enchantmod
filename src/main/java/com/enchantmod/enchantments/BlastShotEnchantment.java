package com.enchantmod.enchantments;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

public class BlastShotEnchantment extends Enchantment {

    public BlastShotEnchantment() {
        super(
            Rarity.VERY_RARE,
            EnchantmentCategory.BOW,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}
        );
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // Не появляется в столе зачарований
        return false;
    }

    @Override
    public int getMaxLevel() { return 2; }

    // Высокая цена — делает объединение дорогим
    @Override
    public int getMinCost(int level) { return 40 + (level - 1) * 30; }

    @Override
    public int getMaxCost(int level) { return getMinCost(level) + 50; }

    // Очень редко у жителей
    @Override
    public boolean isTradeable() { return true; }

    @Override
    public boolean isDiscoverable() { return true; }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other == Enchantments.INFINITY_ARROWS) return false;
        if (other == Enchantments.MULTISHOT) return false;
        if (other == Enchantments.PIERCING) return false;
        return super.checkCompatibility(other);
    }
}

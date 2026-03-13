package com.enchantmod.enchantments;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantments;

public class BlastShotEnchantment extends Enchantment {

    public BlastShotEnchantment() {
        super(
            Rarity.VERY_RARE,
            EnchantmentCategory.BOW,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}
        );
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 50;
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other == Enchantments.INFINITY_ARROWS) return false;
        if (other == Enchantments.MULTISHOT) return false;
        if (other == Enchantments.PIERCING) return false;
        return super.checkCompatibility(other);
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }
}

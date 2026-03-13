package com.enchantmod.enchantments;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public class BloodLeechEnchantment extends Enchantment {

    public BloodLeechEnchantment() {
        super(
            Rarity.VERY_RARE,
            EnchantmentCategory.WEAPON,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND}
        );
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    public int getMaxLevel() { return 3; }

    @Override
    public int getMinCost(int level) { return 20 + (level - 1) * 10; }

    @Override
    public int getMaxCost(int level) { return getMinCost(level) + 50; }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other == Enchantments.MOB_LOOTING) return false;
        return super.checkCompatibility(other);
    }

    @Override
    public boolean isTradeable() { return true; }

    @Override
    public boolean isDiscoverable() { return true; }
}

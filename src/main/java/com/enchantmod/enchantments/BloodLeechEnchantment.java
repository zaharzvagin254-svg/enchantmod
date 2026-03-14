package com.enchantmod.enchantments;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
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

    private static boolean isSword(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof SwordItem) return true;
        if (stack.is(ItemTags.SWORDS)) return true;
        return false;
    }

    @Override
    public boolean canEnchant(ItemStack stack) { return isSword(stack); }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // Не появляется в столе зачарований
        return false;
    }

    @Override
    public int getMaxLevel() { return 3; }

    // Очень высокая цена объединения
    @Override
    public int getMinCost(int level) { return 50 + (level - 1) * 40; }

    @Override
    public int getMaxCost(int level) { return getMinCost(level) + 50; }

    // Не продаётся у жителей
    @Override
    public boolean isTradeable() { return false; }

    // Появляется только в сундуках лута
    @Override
    public boolean isDiscoverable() { return true; }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other == Enchantments.MOB_LOOTING) return false;
        return super.checkCompatibility(other);
    }
}

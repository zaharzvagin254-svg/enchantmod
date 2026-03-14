package com.enchantmod.enchantments;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public class BladeFuryEnchantment extends Enchantment {

    public BladeFuryEnchantment() {
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) { return false; }

    @Override
    public int getMaxLevel() { return 2; }

    // Дорогое объединение
    @Override
    public int getMinCost(int level) { return 45 + (level - 1) * 35; }

    @Override
    public int getMaxCost(int level) { return getMinCost(level) + 50; }

    // Дорого у жителей — как взрывной выстрел
    @Override
    public boolean isTradeable() { return true; }

    @Override
    public boolean isDiscoverable() { return true; }

    // Совместимо со всеми
    @Override
    public boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other);
    }
}

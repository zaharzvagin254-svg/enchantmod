package com.enchantmod.enchantments;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;

public class InfernumEnchantment extends Enchantment {

    public InfernumEnchantment() {
        super(
            Rarity.VERY_RARE,
            EnchantmentCategory.WEAPON,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}
        );
    }

    private static boolean isValidItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof SwordItem) return true;
        if (stack.getItem() instanceof BowItem) return true;
        if (stack.getItem() instanceof CrossbowItem) return true;
        if (stack.is(ItemTags.SWORDS)) return true;
        return false;
    }

    @Override
    public boolean canEnchant(ItemStack stack) { return isValidItem(stack); }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) { return false; }

    @Override
    public int getMaxLevel() { return 1; }

    @Override
    public int getMinCost(int level) { return 50; }

    @Override
    public int getMaxCost(int level) { return 100; }

    @Override
    public boolean isTradeable() { return true; }

    @Override
    public boolean isDiscoverable() { return true; }

    // Инфернум конфликтует с воспламинением и заговором огня -
    // они не работают вместе (Infernum заменяет их)
    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other == Enchantments.FIRE_ASPECT) return false;
        if (other == Enchantments.FLAMING_ARROWS) return false;
        return super.checkCompatibility(other);
    }
}

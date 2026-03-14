package com.enchantmod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = EnchantMod.MOD_ID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);

        String tooltipText = event.getToolTip().stream()
            .map(c -> c.getString())
            .reduce("", String::concat);

        if (enchants.containsKey(ModEnchantments.BLAST_SHOT.get())) {
            String desc = Component.translatable("enchantment.enchantmod.blast_shot.desc").getString();
            if (!tooltipText.contains(desc)) {
                event.getToolTip().add(Component.translatable("enchantment.enchantmod.blast_shot.desc")
                    .withStyle(ChatFormatting.GRAY));
            }
        }
        if (enchants.containsKey(ModEnchantments.VAMPIRISM.get())) {
            String desc = Component.translatable("enchantment.enchantmod.vampirism.desc").getString();
            if (!tooltipText.contains(desc)) {
                event.getToolTip().add(Component.translatable("enchantment.enchantmod.vampirism.desc")
                    .withStyle(ChatFormatting.GRAY));
            }
        }
        if (enchants.containsKey(ModEnchantments.BLADE_FURY.get())) {
            String desc = Component.translatable("enchantment.enchantmod.blade_fury.desc").getString();
            if (!tooltipText.contains(desc)) {
                event.getToolTip().add(Component.translatable("enchantment.enchantmod.blade_fury.desc")
                    .withStyle(ChatFormatting.GRAY));
            }
        }
    }
}

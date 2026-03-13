package com.enchantmod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        Map<Enchantment, Integer> enchants;

        if (stack.is(Items.ENCHANTED_BOOK)) {
            enchants = EnchantmentHelper.getEnchantments(stack);
        } else {
            enchants = EnchantmentHelper.getEnchantments(stack);
        }

        for (Enchantment ench : enchants.keySet()) {
            if (ench == ModEnchantments.BLAST_SHOT.get()) {
                event.getToolTip().add(Component.translatable("enchantment.enchantmod.blast_shot.desc")
                    .withStyle(ChatFormatting.GRAY));
            }
            if (ench == ModEnchantments.BLOOD_LEECH.get()) {
                event.getToolTip().add(Component.translatable("enchantment.enchantmod.blood_leech.desc")
                    .withStyle(ChatFormatting.GRAY));
            }
        }
    }
}

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

        addDesc(event, enchants, ModEnchantments.BLAST_SHOT.get(),
            "enchantment.enchantmod.blast_shot.desc", tooltipText);
        addDesc(event, enchants, ModEnchantments.VAMPIRISM.get(),
            "enchantment.enchantmod.vampirism.desc", tooltipText);
        addDesc(event, enchants, ModEnchantments.BLADE_FURY.get(),
            "enchantment.enchantmod.blade_fury.desc", tooltipText);
        addDesc(event, enchants, ModEnchantments.INFERNUM.get(),
            "enchantment.enchantmod.infernum.desc", tooltipText);
    }

    private static void addDesc(ItemTooltipEvent event, Map<Enchantment, Integer> enchants,
                                  Enchantment ench, String key, String existing) {
        if (!enchants.containsKey(ench)) return;
        String desc = Component.translatable(key).getString();
        if (!existing.contains(desc)) {
            event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }
    }
}

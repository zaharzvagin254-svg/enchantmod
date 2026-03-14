package com.enchantmod.render;

import com.enchantmod.EnchantMod;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = EnchantMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        // Player skins
        for (String skin : event.getSkins()) {
            Object renderer = event.getSkin(skin);
            if (renderer instanceof LivingEntityRenderer r) {
                r.addLayer(new BlueFireRenderLayer(r));
            }
        }

        // All entity types from registry
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            try {
                Object renderer = event.getRenderer((EntityType) type);
                if (renderer instanceof LivingEntityRenderer r) {
                    r.addLayer(new BlueFireRenderLayer(r));
                }
            } catch (Exception ignored) {}
        }
    }
}

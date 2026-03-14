package com.enchantmod.render;

import com.enchantmod.EnchantMod;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnchantMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof LivingEntityRenderer r) {
                r.addLayer(new BlueFireRenderLayer<>(r));
            }
        }

        addLayer(event, EntityType.ZOMBIE);
        addLayer(event, EntityType.SKELETON);
        addLayer(event, EntityType.CREEPER);
        addLayer(event, EntityType.SPIDER);
        addLayer(event, EntityType.CAVE_SPIDER);
        addLayer(event, EntityType.ENDERMAN);
        addLayer(event, EntityType.WITCH);
        addLayer(event, EntityType.HUSK);
        addLayer(event, EntityType.STRAY);
        addLayer(event, EntityType.DROWNED);
        addLayer(event, EntityType.PILLAGER);
        addLayer(event, EntityType.VINDICATOR);
        addLayer(event, EntityType.EVOKER);
        addLayer(event, EntityType.RAVAGER);
        addLayer(event, EntityType.BLAZE);
        addLayer(event, EntityType.GHAST);
        addLayer(event, EntityType.WITHER_SKELETON);
        addLayer(event, EntityType.PIGLIN);
        addLayer(event, EntityType.PIGLIN_BRUTE);
        addLayer(event, EntityType.ZOMBIFIED_PIGLIN);
        addLayer(event, EntityType.GUARDIAN);
        addLayer(event, EntityType.ELDER_GUARDIAN);
        addLayer(event, EntityType.WITHER);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addLayer(EntityRenderersEvent.AddLayers event, EntityType<?> type) {
        var renderer = event.getRenderer((EntityType) type);
        if (renderer instanceof LivingEntityRenderer r) {
            r.addLayer(new BlueFireRenderLayer<>(r));
        }
    }
}

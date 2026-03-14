package com.enchantmod.render;

import com.enchantmod.EnchantMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnchantMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer != null) {
                addLayer(renderer);
            }
        }

        addLayerForType(event, EntityType.ZOMBIE);
        addLayerForType(event, EntityType.SKELETON);
        addLayerForType(event, EntityType.CREEPER);
        addLayerForType(event, EntityType.SPIDER);
        addLayerForType(event, EntityType.CAVE_SPIDER);
        addLayerForType(event, EntityType.ENDERMAN);
        addLayerForType(event, EntityType.WITCH);
        addLayerForType(event, EntityType.HUSK);
        addLayerForType(event, EntityType.STRAY);
        addLayerForType(event, EntityType.DROWNED);
        addLayerForType(event, EntityType.PILLAGER);
        addLayerForType(event, EntityType.VINDICATOR);
        addLayerForType(event, EntityType.EVOKER);
        addLayerForType(event, EntityType.RAVAGER);
        addLayerForType(event, EntityType.BLAZE);
        addLayerForType(event, EntityType.GHAST);
        addLayerForType(event, EntityType.WITHER_SKELETON);
        addLayerForType(event, EntityType.PIGLIN);
        addLayerForType(event, EntityType.PIGLIN_BRUTE);
        addLayerForType(event, EntityType.ZOMBIFIED_PIGLIN);
        addLayerForType(event, EntityType.GUARDIAN);
        addLayerForType(event, EntityType.ELDER_GUARDIAN);
        addLayerForType(event, EntityType.WITHER);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addLayerForType(EntityRenderersEvent.AddLayers event, EntityType<?> type) {
        var renderer = event.getRenderer(type);
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            addLayer(livingRenderer);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addLayer(LivingEntityRenderer renderer) {
        renderer.addLayer(new BlueFireRenderLayer<>(renderer));
    }
}

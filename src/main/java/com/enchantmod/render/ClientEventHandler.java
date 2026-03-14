package com.enchantmod.render;

import com.enchantmod.EnchantMod;
import net.minecraft.client.renderer.entity.EntityRenderer;
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
        // Добавляем слой ко всем living entity типам
        for (EntityType<?> entityType : event.getEntityTypes()) {
            EntityRenderer<?> renderer = event.getRenderer(entityType);
            if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
                addBlueFireLayer(livingRenderer);
            }
        }
        // Также добавляем для игрока
        for (String skin : event.getSkins()) {
            var playerRenderer = event.getSkin(skin);
            if (playerRenderer != null) {
                addBlueFireLayer(playerRenderer);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends LivingEntity, M extends net.minecraft.client.model.EntityModel<T>>
    void addBlueFireLayer(LivingEntityRenderer<T, M> renderer) {
        renderer.addLayer(new BlueFireRenderLayer<>(renderer));
    }
}

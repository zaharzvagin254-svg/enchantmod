package com.enchantmod.render;

import com.enchantmod.EnchantMod;
import com.enchantmod.ModParticles;
import com.enchantmod.particle.BlueSparkParticle;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = EnchantMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.BLUE_SPARK.get(), BlueSparkParticle.Provider::new);
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        int count = 0;
        for (String skin : event.getSkins()) {
            Object renderer = event.getSkin(skin);
            if (renderer instanceof LivingEntityRenderer r) {
                r.addLayer(new BlueFireRenderLayer(r));
                count++;
            }
        }
        for (EntityType type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            try {
                Object renderer = event.getRenderer(type);
                if (renderer instanceof LivingEntityRenderer r) {
                    r.addLayer(new BlueFireRenderLayer(r));
                    count++;
                }
            } catch (Exception ignored) {}
        }
        EnchantMod.LOGGER.info("[EnchantMod] BlueFireRenderLayer added to {} renderers", count);
    }
}

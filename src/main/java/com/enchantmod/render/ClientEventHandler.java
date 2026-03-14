package com.enchantmod.render;

import com.enchantmod.EnchantMod;
import com.enchantmod.ModEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = EnchantMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    // Перехватываем рендер ПЕРЕД отрисовкой сущности
    // Если есть синий огонь - убираем ванильный огонь через setRemainingFireTicks
    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.BLUE_HELLFIRE.get())) {
            // Временно убираем ванильный огонь для рендера
            // Сохраняем тики и восстанавливаем после рендера
            entity.getPersistentData().putInt("infernum_fire_ticks", entity.getRemainingFireTicks());
            entity.setRemainingFireTicks(-1);
        }
    }

    // После рендера восстанавливаем тики огня (для логики сервера)
    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.BLUE_HELLFIRE.get())) {
            int ticks = entity.getPersistentData().getInt("infernum_fire_ticks");
            if (ticks > 0) {
                entity.setRemainingFireTicks(ticks);
            }
        }
    }
}

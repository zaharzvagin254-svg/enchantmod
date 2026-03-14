package com.enchantmod.mixin;

import com.enchantmod.InfernumFire;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(
        method = "renderFlame",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderFlame(PoseStack poseStack, MultiBufferSource bufferSource,
                                T entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity living)) return;
        if (!InfernumFire.hasSoulFire(living)) return;

        // Отменяем обычный огонь
        ci.cancel();

        // Берём текстуры soul fire
        TextureAtlasSprite sprite0 = Minecraft.getInstance()
            .getBlockRenderer().getBlockModelShaper()
            .getParticleIcon(Blocks.SOUL_FIRE.defaultBlockState());
        TextureAtlasSprite sprite1 = sprite0;

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());

        poseStack.pushPose();
        float w = entity.getBbWidth() * 0.8f;
        poseStack.scale(w, w, w);

        for (int i = 0; i < 2; i++) {
            poseStack.pushPose();
            if (i == 1) {
                poseStack.translate(0, 0, -0.001f);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90f));
            }
            TextureAtlasSprite sprite = i == 0 ? sprite0 : sprite1;
            Matrix4f mat = poseStack.last().pose();
            Matrix3f norm = poseStack.last().normal();

            addVertex(consumer, mat, norm, -0.5f, 0.0f, 0.0f, sprite.getU0(), sprite.getV1());
            addVertex(consumer, mat, norm,  0.5f, 0.0f, 0.0f, sprite.getU1(), sprite.getV1());
            addVertex(consumer, mat, norm,  0.5f, 1.4f, 0.0f, sprite.getU1(), sprite.getV0());
            addVertex(consumer, mat, norm, -0.5f, 1.4f, 0.0f, sprite.getU0(), sprite.getV0());

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f mat, Matrix3f norm,
                                   float x, float y, float z, float u, float v) {
        consumer.vertex(mat, x, y, z)
            .color(1.0f, 1.0f, 1.0f, 1.0f)
            .uv(u, v)
            .overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
            .uv2(240)
            .normal(norm, 0.0f, 1.0f, 0.0f)
            .endVertex();
    }
}

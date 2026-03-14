package com.enchantmod.mixin;

import com.enchantmod.InfernumFire;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;renderFlame(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;)V"
        ),
        cancellable = true
    )
    private void onRenderFlame(T entity, float yaw, float partialTick,
                                PoseStack poseStack, MultiBufferSource bufferSource,
                                int packedLight, CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            if (InfernumFire.hasSoulFire(living)) {
                // Отменяем обычный огонь
                ci.cancel();
                // Рендерим soul fire вместо обычного
                renderSoulFlame(poseStack, bufferSource, entity);
            }
        }
    }

    private void renderSoulFlame(PoseStack poseStack, MultiBufferSource bufferSource, T entity) {
        // Используем текстуры soul fire из ванилы
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        net.minecraft.client.renderer.texture.TextureAtlasSprite[] sprites = new net.minecraft.client.renderer.texture.TextureAtlasSprite[]{
            mc.getBlockRenderer().getBlockModelShaper().getParticleIcon(
                net.minecraft.world.level.block.Blocks.SOUL_FIRE.defaultBlockState()
            )
        };

        poseStack.pushPose();
        float size = entity.getBbWidth() * 1.4f;
        poseStack.scale(size, size, size);

        com.mojang.blaze3d.vertex.VertexConsumer consumer = bufferSource.getBuffer(
            net.minecraft.client.renderer.RenderType.cutout()
        );

        net.minecraft.client.renderer.block.model.BakedQuad[] quads = null;

        // Рендерим 2 перекрещенных quad с текстурой soul fire
        for (int i = 0; i < 2; i++) {
            poseStack.pushPose();
            poseStack.translate(-0.5f, 0.0f, 0.0f);
            if (i == 1) poseStack.mulPose(
                com.mojang.math.Axis.YP.rotationDegrees(90f)
            );

            com.mojang.blaze3d.vertex.PoseStack.Pose pose = poseStack.last();

            // Верхний quad
            addFireVertex(consumer, pose, sprites[0], 0, 0, 1, 1.0f, 0.0f);
            addFireVertex(consumer, pose, sprites[0], 1, 0, 1, 1.0f, 0.0f);
            addFireVertex(consumer, pose, sprites[0], 1, 1, 1, 0.0f, 0.0f);
            addFireVertex(consumer, pose, sprites[0], 0, 1, 1, 0.0f, 0.0f);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void addFireVertex(com.mojang.blaze3d.vertex.VertexConsumer consumer,
                                com.mojang.blaze3d.vertex.PoseStack.Pose pose,
                                net.minecraft.client.renderer.texture.TextureAtlasSprite sprite,
                                float x, float y, float z, float u, float v) {
        consumer.vertex(pose.pose(), x - 0.5f, y, z)
            .color(1.0f, 1.0f, 1.0f, 1.0f)
            .uv(sprite.getU(u), sprite.getV(v))
            .overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
            .uv2(240)
            .normal(pose.normal(), 0, 1, 0)
            .endVertex();
    }
}

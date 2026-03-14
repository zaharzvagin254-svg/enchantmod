package com.enchantmod.render;

import com.enchantmod.ModEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BlueFireRenderLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    private static final ResourceLocation FIRE_0 =
        new ResourceLocation("minecraft", "textures/block/soul_fire_0.png");
    private static final ResourceLocation FIRE_1 =
        new ResourceLocation("minecraft", "textures/block/soul_fire_1.png");

    public BlueFireRenderLayer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        if (!entity.hasEffect(ModEffects.BLUE_HELLFIRE.get())) return;

        ResourceLocation[] textures = new ResourceLocation[]{FIRE_0, FIRE_1};

        poseStack.pushPose();

        float w = entity.getBbWidth() * 1.0f;
        float h = entity.getBbHeight() + 0.5f;

        poseStack.scale(w, h, w);
        poseStack.translate(0.0, 0.0, -0.3);

        for (int i = 0; i < 2; i++) {
            poseStack.pushPose();
            if (i == 1) {
                poseStack.mulPose(Axis.YP.rotationDegrees(90f));
            }

            ResourceLocation texture = textures[i % 2];
            VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(texture)
            );

            PoseStack.Pose pose = poseStack.last();
            Matrix4f mat = pose.pose();
            Matrix3f norm = pose.normal();

            addQuad(consumer, mat, norm, -0.5f, 0.0f, 0.5f, 0.0f, 1.0f, packedLight);
            addQuad(consumer, mat, norm, -0.5f, 0.5f, 0.5f, 1.0f, 0.0f, packedLight);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void addQuad(VertexConsumer consumer, Matrix4f mat, Matrix3f norm,
                          float x1, float y1, float x2, float y2, float v,
                          int packedLight) {
        consumer.vertex(mat, x1, y1, 0).color(1f,1f,1f,1f).uv(1.0f, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(norm, 0,1,0).endVertex();
        consumer.vertex(mat, x2, y1, 0).color(1f,1f,1f,1f).uv(0.0f, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(norm, 0,1,0).endVertex();
        consumer.vertex(mat, x2, y2, 0).color(1f,1f,1f,1f).uv(0.0f, 1f-v)
            .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(norm, 0,1,0).endVertex();
        consumer.vertex(mat, x1, y2, 0).color(1f,1f,1f,1f).uv(1.0f, 1f-v)
            .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(norm, 0,1,0).endVertex();
    }
}

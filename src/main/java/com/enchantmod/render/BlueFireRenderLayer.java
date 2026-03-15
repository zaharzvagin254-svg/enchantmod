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
        new ResourceLocation("enchantmod", "textures/block/blue_fire_0.png");
    private static final ResourceLocation FIRE_1 =
        new ResourceLocation("enchantmod", "textures/block/blue_fire_1.png");

    public BlueFireRenderLayer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        if (!entity.hasEffect(ModEffects.BLUE_HELLFIRE.get())) return;

        poseStack.pushPose();

        float w = entity.getBbWidth() * 1.4f;
        float h = entity.getBbHeight() + 0.5f;
        poseStack.scale(w, h, w);

        renderSlice(poseStack, bufferSource, FIRE_0, 0f, packedLight);
        renderSlice(poseStack, bufferSource, FIRE_1, 90f, packedLight);

        poseStack.popPose();
    }

    private void renderSlice(PoseStack poseStack, MultiBufferSource bufferSource,
                               ResourceLocation texture, float yRot, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.translate(0, 0, yRot > 0 ? -0.001f : 0.001f);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        Matrix3f norm = pose.normal();

        vertex(consumer, mat, norm, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, packedLight);
        vertex(consumer, mat, norm,  0.5f, 0.0f, 0.0f, 1.0f, 1.0f, packedLight);
        vertex(consumer, mat, norm,  0.5f, 0.5f, 0.0f, 1.0f, 0.5f, packedLight);
        vertex(consumer, mat, norm, -0.5f, 0.5f, 0.0f, 0.0f, 0.5f, packedLight);

        vertex(consumer, mat, norm, -0.5f, 0.5f, 0.0f, 0.0f, 0.5f, packedLight);
        vertex(consumer, mat, norm,  0.5f, 0.5f, 0.0f, 1.0f, 0.5f, packedLight);
        vertex(consumer, mat, norm,  0.5f, 1.0f, 0.0f, 1.0f, 0.0f, packedLight);
        vertex(consumer, mat, norm, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, packedLight);

        poseStack.popPose();
    }

    private void vertex(VertexConsumer c, Matrix4f mat, Matrix3f norm,
                         float x, float y, float z, float u, float v, int light) {
        c.vertex(mat, x, y, z)
            .color(1f, 1f, 1f, 1f)
            .uv(u, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(norm, 0, 1, 0)
            .endVertex();
    }
}

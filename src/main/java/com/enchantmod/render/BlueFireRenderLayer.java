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

        // Exactly how vanilla EntityRenderer renders fire on entities
        // Copied from net.minecraft.client.renderer.entity.EntityRenderer#renderFlame
        poseStack.pushPose();

        float scaleXZ = entity.getBbWidth() * 1.4f;
        float scaleY = entity.getBbHeight() + 0.5f;

        poseStack.scale(scaleXZ, scaleY, scaleXZ);

        float f = 0.5f;
        float f1 = entity.getBbHeight() / scaleY;

        renderQuads(poseStack, bufferSource, FIRE_0, f, f1, packedLight, 0.0f);
        renderQuads(poseStack, bufferSource, FIRE_1, f, f1, packedLight, 90.0f);

        poseStack.popPose();
    }

    private static void renderQuads(PoseStack poseStack, MultiBufferSource bufferSource,
                                      ResourceLocation texture, float f, float f1,
                                      int packedLight, float yRotDeg) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotDeg));

        float zOffset = -0.5f + (yRotDeg > 0 ? 0.001f : 0.0f);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        Matrix3f norm = pose.normal();

        VertexConsumer consumer = bufferSource.getBuffer(
            RenderType.entityCutoutNoCull(texture)
        );

        // Lower quad
        addVertex(consumer, mat, norm, -f, 0f,    zOffset, 0f, f1,   packedLight);
        addVertex(consumer, mat, norm,  f, 0f,    zOffset, 1f, f1,   packedLight);
        addVertex(consumer, mat, norm,  f, 1f,    zOffset, 1f, 0f,   packedLight);
        addVertex(consumer, mat, norm, -f, 1f,    zOffset, 0f, 0f,   packedLight);

        // Upper quad (offset upward)
        addVertex(consumer, mat, norm, -f, -0.5f, zOffset, 0f, f1,   packedLight);
        addVertex(consumer, mat, norm,  f, -0.5f, zOffset, 1f, f1,   packedLight);
        addVertex(consumer, mat, norm,  f,  0.5f, zOffset, 1f, 0f,   packedLight);
        addVertex(consumer, mat, norm, -f,  0.5f, zOffset, 0f, 0f,   packedLight);

        poseStack.popPose();
    }

    private static void addVertex(VertexConsumer c, Matrix4f mat, Matrix3f norm,
                                    float x, float y, float z, float u, float v, int light) {
        c.vertex(mat, x, y, z)
            .color(1f, 1f, 1f, 1f)
            .uv(u, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(norm, 0f, 1f, 0f)
            .endVertex();
    }
}

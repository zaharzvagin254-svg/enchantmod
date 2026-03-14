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

        float scaleX = entity.getBbWidth() * 1.4f;
        float scaleY = entity.getBbHeight() + 0.5f;

        poseStack.scale(scaleX, scaleY, scaleX);

        // Точно как ванильный FireLayer рендерит огонь
        float f = 0.5f;
        float f1 = entity.getBbHeight() / scaleY;

        renderFire(poseStack, bufferSource, FIRE_0, f, f1, packedLight, 0.0f);
        renderFire(poseStack, bufferSource, FIRE_1, f, f1, packedLight, 90.0f);

        poseStack.popPose();
    }

    private void renderFire(PoseStack poseStack, MultiBufferSource bufferSource,
                             ResourceLocation texture, float f, float f1,
                             int packedLight, float rotation) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        float x0 = -f;
        float x1 = f;
        float y0 = 0.0f;
        float y1 = 1.0f;
        float z = 0.001f - (rotation > 0 ? 0.001f : 0.0f);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        Matrix3f norm = pose.normal();

        // Bottom quad
        addVertex(consumer, mat, norm, x0, y0, z, 0, f1, packedLight);
        addVertex(consumer, mat, norm, x1, y0, z, 1, f1, packedLight);
        addVertex(consumer, mat, norm, x1, y1, z, 1, 0, packedLight);
        addVertex(consumer, mat, norm, x0, y1, z, 0, 0, packedLight);

        // Back face
        addVertex(consumer, mat, norm, x1, y0, z, 0, f1, packedLight);
        addVertex(consumer, mat, norm, x0, y0, z, 1, f1, packedLight);
        addVertex(consumer, mat, norm, x0, y1, z, 1, 0, packedLight);
        addVertex(consumer, mat, norm, x1, y1, z, 0, 0, packedLight);

        poseStack.popPose();
    }

    private void addVertex(VertexConsumer consumer, Matrix4f mat, Matrix3f norm,
                            float x, float y, float z, float u, float v, int packedLight) {
        consumer.vertex(mat, x - 0.5f, y, z)
            .color(1f, 1f, 1f, 1f)
            .uv(u, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(norm, 0, 1, 0)
            .endVertex();
    }
}

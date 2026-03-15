package com.enchantmod.render;

import com.enchantmod.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

        Tesselator tesselator = Tesselator.getInstance();

        poseStack.pushPose();

        float w = entity.getBbWidth() * 1.4f;
        float h = entity.getBbHeight() + 0.5f;
        poseStack.scale(w, h, w);

        renderFirePass(poseStack, tesselator, FIRE_0, 0f);
        renderFirePass(poseStack, tesselator, FIRE_1, 90f);

        poseStack.popPose();
    }

    private void renderFirePass(PoseStack poseStack, Tesselator tesselator,
                                  ResourceLocation texture, float yRot) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f mat = poseStack.last().pose();
        BufferBuilder buf = tesselator.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        buf.vertex(mat, -0.5f, 0.0f, 0.0f).uv(0f, 1f).endVertex();
        buf.vertex(mat,  0.5f, 0.0f, 0.0f).uv(1f, 1f).endVertex();
        buf.vertex(mat,  0.5f, 0.5f, 0.0f).uv(1f, 0.5f).endVertex();
        buf.vertex(mat, -0.5f, 0.5f, 0.0f).uv(0f, 0.5f).endVertex();

        buf.vertex(mat, -0.5f, 0.5f, 0.0f).uv(0f, 0.5f).endVertex();
        buf.vertex(mat,  0.5f, 0.5f, 0.0f).uv(1f, 0.5f).endVertex();
        buf.vertex(mat,  0.5f, 1.0f, 0.0f).uv(1f, 0f).endVertex();
        buf.vertex(mat, -0.5f, 1.0f, 0.0f).uv(0f, 0f).endVertex();

        tesselator.end();

        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}

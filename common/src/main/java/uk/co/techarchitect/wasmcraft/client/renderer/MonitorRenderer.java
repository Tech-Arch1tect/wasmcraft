package uk.co.techarchitect.wasmcraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import org.joml.Matrix4f;
import uk.co.techarchitect.wasmcraft.blockentity.MonitorBlockEntity;

import java.util.HashMap;
import java.util.Map;

public class MonitorRenderer implements BlockEntityRenderer<MonitorBlockEntity> {
    private static final Map<MonitorBlockEntity, MonitorTexture> TEXTURES = new HashMap<>();
    private static final int RESOLUTION = 64;

    private static class MonitorTexture {
        final DynamicTexture texture;
        final ResourceLocation location;

        MonitorTexture() {
            texture = new DynamicTexture(RESOLUTION, RESOLUTION, true);
            location = Minecraft.getInstance().getTextureManager().register("monitor", texture);
        }

        void close() {
            texture.close();
        }
    }

    public MonitorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MonitorBlockEntity monitor, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        Direction facing = monitor.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        MonitorTexture monitorTex = TEXTURES.computeIfAbsent(monitor, m -> new MonitorTexture());

        byte[] pixelData = monitor.getPixelData();
        for (int y = 0; y < RESOLUTION; y++) {
            for (int x = 0; x < RESOLUTION; x++) {
                int index = (y * RESOLUTION + x) * 3;
                int r = pixelData[index] & 0xFF;
                int g = pixelData[index + 1] & 0xFF;
                int b = pixelData[index + 2] & 0xFF;
                int abgr = 0xFF000000 | (b << 16) | (g << 8) | r;
                monitorTex.texture.getPixels().setPixelRGBA(x, y, abgr);
            }
        }


        monitorTex.texture.upload();

        poseStack.pushPose();

        switch (facing) {
            case NORTH -> {
                poseStack.translate(0, 0, -0.01);
            }
            case SOUTH -> {
                poseStack.translate(1, 0, 1.01);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
            }
            case WEST -> {
                poseStack.translate(-0.01, 0, 1);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
            }
            case EAST -> {
                poseStack.translate(1.01, 0, 0);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
            }
        }

        RenderSystem.setShaderTexture(0, monitorTex.location);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.textSeeThrough(monitorTex.location));
        Matrix4f matrix = poseStack.last().pose();

        consumer.addVertex(matrix, 0, 1, 0).setColor(255, 255, 255, 255).setUv(0, 0).setLight(light);
        consumer.addVertex(matrix, 1, 1, 0).setColor(255, 255, 255, 255).setUv(1, 0).setLight(light);
        consumer.addVertex(matrix, 1, 0, 0).setColor(255, 255, 255, 255).setUv(1, 1).setLight(light);
        consumer.addVertex(matrix, 0, 0, 0).setColor(255, 255, 255, 255).setUv(0, 1).setLight(light);

        poseStack.popPose();
    }
}

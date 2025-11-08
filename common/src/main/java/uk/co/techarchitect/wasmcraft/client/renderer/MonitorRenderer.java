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

    private static class MonitorTexture {
        DynamicTexture texture;
        ResourceLocation location;
        int resolution;

        MonitorTexture(int resolution) {
            this.resolution = resolution;
            texture = new DynamicTexture(resolution, resolution, true);
            location = Minecraft.getInstance().getTextureManager().register("monitor", texture);
        }

        void updateResolution(int newResolution) {
            if (this.resolution != newResolution) {
                texture.close();
                this.resolution = newResolution;
                texture = new DynamicTexture(newResolution, newResolution, true);
                location = Minecraft.getInstance().getTextureManager().register("monitor", texture);
            }
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

        // Get the controller to access the unified pixel buffer
        MonitorBlockEntity controller = monitor.getController();
        if (controller == null) {
            return;
        }

        int resolution = controller.getResolution();
        MonitorTexture monitorTex = TEXTURES.computeIfAbsent(monitor, m -> new MonitorTexture(resolution));
        monitorTex.updateResolution(resolution);

        byte[] pixelData = controller.getPixelData();
        if (pixelData == null) {
            return;
        }

        int[] offset = monitor.getOffsetInStructure();
        int offsetX = offset[0];
        int offsetY = offset[1];
        int bufferWidth = controller.getPixelWidth();

        for (int y = 0; y < resolution; y++) {
            for (int x = 0; x < resolution; x++) {
                int globalX = offsetX + x;
                int globalY = offsetY + y;
                int index = (globalY * bufferWidth + globalX) * 3;

                if (index >= 0 && index + 2 < pixelData.length) {
                    int r = pixelData[index] & 0xFF;
                    int g = pixelData[index + 1] & 0xFF;
                    int b = pixelData[index + 2] & 0xFF;

                    int abgr = 0xFF000000 | (b << 16) | (g << 8) | r;
                    int texX = resolution - 1 - x;
                    monitorTex.texture.getPixels().setPixelRGBA(texX, y, abgr);
                }
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

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(monitorTex.location));
        Matrix4f matrix = poseStack.last().pose();

        consumer.addVertex(matrix, 0, 1, 0).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(overlay).setLight(15728880).setNormal(0, 0, 1);
        consumer.addVertex(matrix, 1, 1, 0).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(overlay).setLight(15728880).setNormal(0, 0, 1);
        consumer.addVertex(matrix, 1, 0, 0).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(overlay).setLight(15728880).setNormal(0, 0, 1);
        consumer.addVertex(matrix, 0, 0, 0).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(overlay).setLight(15728880).setNormal(0, 0, 1);

        poseStack.popPose();
    }
}

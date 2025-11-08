package uk.co.techarchitect.wasmcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.techarchitect.wasmcraft.network.ModNetworking;
import uk.co.techarchitect.wasmcraft.network.packet.MonitorUpdatePacket;
import uk.co.techarchitect.wasmcraft.peripheral.PeripheralBlockEntity;

import java.util.Random;

public class MonitorBlockEntity extends PeripheralBlockEntity {
    private static final int RESOLUTION = 64;
    private static final int PIXEL_COUNT = RESOLUTION * RESOLUTION;
    private static final long UPDATE_INTERVAL_MS = 50;

    private final byte[] pixels;

    private int dirtyMinX = RESOLUTION;
    private int dirtyMinY = RESOLUTION;
    private int dirtyMaxX = -1;
    private int dirtyMaxY = -1;
    private long lastUpdateTime = 0;

    public MonitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MONITOR_BLOCK_ENTITY.get(), pos, state);
        this.pixels = new byte[PIXEL_COUNT * 3];
    }

    @Override
    protected String generateDefaultLabel() {
        return "monitor_" + Integer.toHexString(new Random().nextInt(0xFFFF));
    }

    @Override
    public String getPeripheralType() {
        return "monitor";
    }

    public int getResolution() {
        return RESOLUTION;
    }

    public void setPixel(int x, int y, int r, int g, int b) {
        if (x < 0 || x >= RESOLUTION || y < 0 || y >= RESOLUTION) {
            return;
        }

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        int index = (y * RESOLUTION + x) * 3;
        pixels[index] = (byte) r;
        pixels[index + 1] = (byte) g;
        pixels[index + 2] = (byte) b;

        markDirty(x, y);

        if (level != null && !level.isClientSide) {
            level.getServer().execute(this::setChanged);
        }
    }

    public int[] getPixel(int x, int y) {
        if (x < 0 || x >= RESOLUTION || y < 0 || y >= RESOLUTION) {
            return new int[]{0, 0, 0};
        }

        int index = (y * RESOLUTION + x) * 3;
        int r = pixels[index] & 0xFF;
        int g = pixels[index + 1] & 0xFF;
        int b = pixels[index + 2] & 0xFF;

        return new int[]{r, g, b};
    }

    public void clear(int r, int g, int b) {
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        for (int i = 0; i < PIXEL_COUNT; i++) {
            int index = i * 3;
            pixels[index] = (byte) r;
            pixels[index + 1] = (byte) g;
            pixels[index + 2] = (byte) b;
        }

        markDirty(0, 0);
        markDirty(RESOLUTION - 1, RESOLUTION - 1);

        if (level != null && !level.isClientSide) {
            level.getServer().execute(this::setChanged);
        }
    }

    public byte[] getPixelData() {
        return pixels;
    }

    private void markDirty(int x, int y) {
        dirtyMinX = Math.min(dirtyMinX, x);
        dirtyMinY = Math.min(dirtyMinY, y);
        dirtyMaxX = Math.max(dirtyMaxX, x);
        dirtyMaxY = Math.max(dirtyMaxY, y);
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (dirtyMaxX >= 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime >= UPDATE_INTERVAL_MS) {
                sendUpdate();
            }
        }
    }

    private void sendUpdate() {
        if (dirtyMaxX < 0) {
            return;
        }

        int width = dirtyMaxX - dirtyMinX + 1;
        int height = dirtyMaxY - dirtyMinY + 1;
        byte[] dirtyData = new byte[width * height * 3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int srcIndex = ((dirtyMinY + y) * RESOLUTION + (dirtyMinX + x)) * 3;
                int dstIndex = (y * width + x) * 3;
                dirtyData[dstIndex] = pixels[srcIndex];
                dirtyData[dstIndex + 1] = pixels[srcIndex + 1];
                dirtyData[dstIndex + 2] = pixels[srcIndex + 2];
            }
        }

        MonitorUpdatePacket packet = new MonitorUpdatePacket(worldPosition, dirtyMinX, dirtyMinY, dirtyMaxX, dirtyMaxY, dirtyData);
        ModNetworking.sendToAllTracking((ServerLevel) level, worldPosition, packet);

        dirtyMinX = RESOLUTION;
        dirtyMinY = RESOLUTION;
        dirtyMaxX = -1;
        dirtyMaxY = -1;
        lastUpdateTime = System.currentTimeMillis();
    }

    public void applyUpdate(int minX, int minY, int maxX, int maxY, byte[] data) {
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int srcIndex = (y * width + x) * 3;
                int dstIndex = ((minY + y) * RESOLUTION + (minX + x)) * 3;
                pixels[dstIndex] = data[srcIndex];
                pixels[dstIndex + 1] = data[srcIndex + 1];
                pixels[dstIndex + 2] = data[srcIndex + 2];
            }
        }
    }
}

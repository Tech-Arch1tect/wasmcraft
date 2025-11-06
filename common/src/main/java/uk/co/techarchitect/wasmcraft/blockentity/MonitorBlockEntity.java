package uk.co.techarchitect.wasmcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.techarchitect.wasmcraft.peripheral.PeripheralBlockEntity;

import java.util.Random;

public class MonitorBlockEntity extends PeripheralBlockEntity {
    private static final int RESOLUTION = 64;
    private static final int PIXEL_COUNT = RESOLUTION * RESOLUTION;

    private final byte[] pixels;

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

        setChanged();
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

        setChanged();
    }

    public byte[] getPixelData() {
        return pixels;
    }
}

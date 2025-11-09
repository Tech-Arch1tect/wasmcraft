package uk.co.techarchitect.wasmcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.techarchitect.wasmcraft.network.ModNetworking;
import uk.co.techarchitect.wasmcraft.network.packet.MonitorUpdatePacket;
import uk.co.techarchitect.wasmcraft.peripheral.PeripheralBlockEntity;

import java.util.*;

public class MonitorBlockEntity extends PeripheralBlockEntity {
    private static final int DEFAULT_RESOLUTION = 64;
    private static final int MIN_RESOLUTION = 16;
    private static final int MAX_RESOLUTION = 128;
    private static final long UPDATE_INTERVAL_MS = 50;

    private int resolution = DEFAULT_RESOLUTION;
    private byte[] pixels;

    private boolean isController = true;
    private BlockPos controllerPos = null;
    private BlockPos structureOrigin = null;
    private int structureWidth = 1;
    private int structureHeight = 1;
    private Set<BlockPos> structureBlocks = new HashSet<>();

    private int gridX = 0;
    private int gridY = 0;

    private int dirtyMinX = Integer.MAX_VALUE;
    private int dirtyMinY = Integer.MAX_VALUE;
    private int dirtyMaxX = -1;
    private int dirtyMaxY = -1;
    private long lastUpdateTime = 0;

    public MonitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MONITOR_BLOCK_ENTITY.get(), pos, state);
        this.pixels = new byte[resolution * resolution * 3];
        this.structureBlocks.add(pos);
    }

    @Override
    protected String generateDefaultLabel() {
        return "monitor_" + Integer.toHexString(new Random().nextInt(0xFFFF));
    }

    @Override
    public String getPeripheralType() {
        return "monitor";
    }

    @Override
    public void setLabel(String label) {
        if (!isController && (structureWidth > 1 || structureHeight > 1)) {
            promoteToController();
        }
        super.setLabel(label);
    }

    private void promoteToController() {
        if (level == null || level.isClientSide) {
            return;
        }

        MonitorBlockEntity oldController = getController();
        if (oldController == null) {
            isController = true;
            controllerPos = null;
            structureWidth = 1;
            structureHeight = 1;
            pixels = new byte[resolution * resolution * 3];
            structureBlocks.clear();
            structureBlocks.add(worldPosition);
            setChanged();
            return;
        }

        Set<BlockPos> allBlocks = new HashSet<>(oldController.structureBlocks);
        int width = oldController.structureWidth;
        int height = oldController.structureHeight;
        byte[] oldPixels = oldController.pixels;
        BlockPos origin = oldController.structureOrigin;
        int oldResolution = oldController.resolution;

        this.isController = true;
        this.controllerPos = null;
        this.structureOrigin = origin;
        this.structureWidth = width;
        this.structureHeight = height;
        this.resolution = oldResolution;
        this.structureBlocks = new HashSet<>(allBlocks);

        if (oldPixels != null) {
            this.pixels = oldPixels.clone();
        } else {
            this.pixels = new byte[width * height * resolution * resolution * 3];
        }

        for (BlockPos pos : allBlocks) {
            if (!pos.equals(worldPosition)) {
                if (level.getBlockEntity(pos) instanceof MonitorBlockEntity member) {
                    member.isController = false;
                    member.controllerPos = worldPosition;
                    member.structureOrigin = origin;
                    member.structureWidth = width;
                    member.structureHeight = height;
                    member.resolution = oldResolution;
                    member.pixels = null;
                    member.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }

        if (!oldController.worldPosition.equals(worldPosition)) {
            oldController.isController = false;
            oldController.controllerPos = worldPosition;
            oldController.pixels = null;
            oldController.setChanged();
            level.sendBlockUpdated(oldController.worldPosition, level.getBlockState(oldController.worldPosition), level.getBlockState(oldController.worldPosition), 3);
        }

        setChanged();
        level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3);
    }

    public int getPixelWidth() {
        return structureWidth * resolution;
    }

    public int getPixelHeight() {
        return structureHeight * resolution;
    }

    public int getResolution() {
        return resolution;
    }

    public boolean isController() {
        return isController;
    }

    public MonitorBlockEntity getController() {
        if (isController) {
            return this;
        }
        if (level != null && controllerPos != null) {
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(controllerPos);
            if (be instanceof MonitorBlockEntity controller) {
                return controller;
            }
        }
        return null;
    }

    private void calculateGridPosition(BlockPos origin, int gridWidth, int gridHeight, String planeType) {
        int dx = worldPosition.getX() - origin.getX();
        int dy = worldPosition.getY() - origin.getY();
        int dz = worldPosition.getZ() - origin.getZ();

        switch (planeType) {
            case "YZ_EAST" -> {
                gridX = -dz;
                gridY = -dy;
            }
            case "YZ_WEST" -> {
                gridX = dz;
                gridY = -dy;
            }
            case "XZ" -> {
                gridX = -dx;
                gridY = -dz;
            }
            case "XY_SOUTH" -> {
                gridX = dx;
                gridY = -dy;
            }
            case "XY_NORTH" -> {
                gridX = -dx;
                gridY = -dy;
            }
            default -> {
                gridX = 0;
                gridY = 0;
            }
        }
    }

    public int[] getOffsetInStructure() {
        MonitorBlockEntity controller = getController();
        int effectiveResolution = controller != null ? controller.resolution : resolution;
        return new int[]{gridX * effectiveResolution, gridY * effectiveResolution};
    }

    public void setPixel(int x, int y, int r, int g, int b) {
        MonitorBlockEntity controller = getController();
        if (controller == null) {
            return;
        }

        byte[] buffer = controller.pixels;
        if (buffer == null) {
            return;
        }

        int width = controller.getPixelWidth();
        int height = controller.getPixelHeight();

        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        int index = (y * width + x) * 3;
        buffer[index] = (byte) r;
        buffer[index + 1] = (byte) g;
        buffer[index + 2] = (byte) b;

        controller.markDirty(x, y);

        if (level != null && !level.isClientSide) {
            level.getServer().execute(controller::setChanged);
        }
    }

    public int[] getPixel(int x, int y) {
        MonitorBlockEntity controller = getController();
        if (controller == null) {
            return new int[]{0, 0, 0};
        }

        byte[] buffer = controller.pixels;
        if (buffer == null) {
            return new int[]{0, 0, 0};
        }

        int width = controller.getPixelWidth();
        int height = controller.getPixelHeight();

        if (x < 0 || x >= width || y < 0 || y >= height) {
            return new int[]{0, 0, 0};
        }

        int index = (y * width + x) * 3;
        int r = buffer[index] & 0xFF;
        int g = buffer[index + 1] & 0xFF;
        int b = buffer[index + 2] & 0xFF;

        return new int[]{r, g, b};
    }

    public void clear(int r, int g, int b) {
        MonitorBlockEntity controller = getController();
        if (controller == null) {
            return;
        }

        byte[] buffer = controller.pixels;
        if (buffer == null) {
            return;
        }

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        int totalPixels = controller.getPixelWidth() * controller.getPixelHeight();

        for (int i = 0; i < totalPixels; i++) {
            int index = i * 3;
            buffer[index] = (byte) r;
            buffer[index + 1] = (byte) g;
            buffer[index + 2] = (byte) b;
        }

        controller.markDirty(0, 0);
        controller.markDirty(controller.getPixelWidth() - 1, controller.getPixelHeight() - 1);

        if (level != null && !level.isClientSide) {
            level.getServer().execute(controller::setChanged);
        }
    }

    public void fillRect(int x, int y, int width, int height, int r, int g, int b) {
        MonitorBlockEntity controller = getController();
        if (controller == null) {
            return;
        }

        byte[] buffer = controller.pixels;
        if (buffer == null) {
            return;
        }

        int bufferWidth = controller.getPixelWidth();
        int bufferHeight = controller.getPixelHeight();

        uk.co.techarchitect.wasmcraft.drawing.MonitorDrawing.fillRect(
            buffer, bufferWidth, bufferHeight, x, y, width, height, r, g, b
        );

        int x1 = Math.max(0, Math.min(bufferWidth - 1, x));
        int y1 = Math.max(0, Math.min(bufferHeight - 1, y));
        int x2 = Math.max(0, Math.min(bufferWidth - 1, x + width - 1));
        int y2 = Math.max(0, Math.min(bufferHeight - 1, y + height - 1));

        controller.markDirty(x1, y1);
        controller.markDirty(x2, y2);

        if (level != null && !level.isClientSide) {
            level.getServer().execute(controller::setChanged);
        }
    }

    public void drawHLine(int x, int y, int length, int r, int g, int b) {
        MonitorBlockEntity controller = getController();
        if (controller == null) {
            return;
        }

        byte[] buffer = controller.pixels;
        if (buffer == null) {
            return;
        }

        int bufferWidth = controller.getPixelWidth();
        int bufferHeight = controller.getPixelHeight();

        uk.co.techarchitect.wasmcraft.drawing.MonitorDrawing.drawHLine(
            buffer, bufferWidth, bufferHeight, x, y, length, r, g, b
        );

        int x1 = Math.max(0, Math.min(bufferWidth - 1, x));
        int y1 = Math.max(0, Math.min(bufferHeight - 1, y));
        int x2 = Math.max(0, Math.min(bufferWidth - 1, x + length - 1));

        controller.markDirty(x1, y1);
        controller.markDirty(x2, y1);

        if (level != null && !level.isClientSide) {
            level.getServer().execute(controller::setChanged);
        }
    }

    public void drawVLine(int x, int y, int length, int r, int g, int b) {
        MonitorBlockEntity controller = getController();
        if (controller == null) {
            return;
        }

        byte[] buffer = controller.pixels;
        if (buffer == null) {
            return;
        }

        int bufferWidth = controller.getPixelWidth();
        int bufferHeight = controller.getPixelHeight();

        uk.co.techarchitect.wasmcraft.drawing.MonitorDrawing.drawVLine(
            buffer, bufferWidth, bufferHeight, x, y, length, r, g, b
        );

        int x1 = Math.max(0, Math.min(bufferWidth - 1, x));
        int y1 = Math.max(0, Math.min(bufferHeight - 1, y));
        int y2 = Math.max(0, Math.min(bufferHeight - 1, y + length - 1));

        controller.markDirty(x1, y1);
        controller.markDirty(x1, y2);

        if (level != null && !level.isClientSide) {
            level.getServer().execute(controller::setChanged);
        }
    }

    public void drawRect(int x, int y, int width, int height, int r, int g, int b) {
        MonitorBlockEntity controller = getController();
        if (controller == null) {
            return;
        }

        byte[] buffer = controller.pixels;
        if (buffer == null) {
            return;
        }

        int bufferWidth = controller.getPixelWidth();
        int bufferHeight = controller.getPixelHeight();

        uk.co.techarchitect.wasmcraft.drawing.MonitorDrawing.drawRect(
            buffer, bufferWidth, bufferHeight, x, y, width, height, r, g, b
        );

        int x1 = Math.max(0, Math.min(bufferWidth - 1, x));
        int y1 = Math.max(0, Math.min(bufferHeight - 1, y));
        int x2 = Math.max(0, Math.min(bufferWidth - 1, x + width - 1));
        int y2 = Math.max(0, Math.min(bufferHeight - 1, y + height - 1));

        controller.markDirty(x1, y1);
        controller.markDirty(x2, y2);

        if (level != null && !level.isClientSide) {
            level.getServer().execute(controller::setChanged);
        }
    }

    public void drawChar(int x, int y, char c, int fgR, int fgG, int fgB, int bgR, int bgG, int bgB, int scale) {
        MonitorBlockEntity controller = getController();
        if (controller == null) {
            return;
        }

        byte[] buffer = controller.pixels;
        if (buffer == null) {
            return;
        }

        int bufferWidth = controller.getPixelWidth();
        int bufferHeight = controller.getPixelHeight();

        uk.co.techarchitect.wasmcraft.drawing.MonitorDrawing.drawChar(
            buffer, bufferWidth, bufferHeight, x, y, c, fgR, fgG, fgB, bgR, bgG, bgB, scale
        );

        int charWidth = uk.co.techarchitect.wasmcraft.drawing.BitmapFont.getCharWidth(scale);
        int charHeight = uk.co.techarchitect.wasmcraft.drawing.BitmapFont.getCharHeight(scale);

        int x1 = Math.max(0, Math.min(bufferWidth - 1, x));
        int y1 = Math.max(0, Math.min(bufferHeight - 1, y));
        int x2 = Math.max(0, Math.min(bufferWidth - 1, x + charWidth - 1));
        int y2 = Math.max(0, Math.min(bufferHeight - 1, y + charHeight - 1));

        controller.markDirty(x1, y1);
        controller.markDirty(x2, y2);

        if (level != null && !level.isClientSide) {
            level.getServer().execute(controller::setChanged);
        }
    }

    public int drawText(int x, int y, String text, int fgR, int fgG, int fgB, int bgR, int bgG, int bgB, int scale) {
        MonitorBlockEntity controller = getController();
        if (controller == null) {
            return 0;
        }

        byte[] buffer = controller.pixels;
        if (buffer == null) {
            return 0;
        }

        int bufferWidth = controller.getPixelWidth();
        int bufferHeight = controller.getPixelHeight();

        int width = uk.co.techarchitect.wasmcraft.drawing.MonitorDrawing.drawText(
            buffer, bufferWidth, bufferHeight, x, y, text, fgR, fgG, fgB, bgR, bgG, bgB, scale
        );

        int textHeight = uk.co.techarchitect.wasmcraft.drawing.BitmapFont.getCharHeight(scale);

        int x1 = Math.max(0, Math.min(bufferWidth - 1, x));
        int y1 = Math.max(0, Math.min(bufferHeight - 1, y));
        int x2 = Math.max(0, Math.min(bufferWidth - 1, x + width - 1));
        int y2 = Math.max(0, Math.min(bufferHeight - 1, y + textHeight - 1));

        controller.markDirty(x1, y1);
        controller.markDirty(x2, y2);

        if (level != null && !level.isClientSide) {
            level.getServer().execute(controller::setChanged);
        }

        return width;
    }

    public int[] measureText(String text, int scale) {
        int width = uk.co.techarchitect.wasmcraft.drawing.MonitorDrawing.measureTextWidth(text, scale);
        int height = uk.co.techarchitect.wasmcraft.drawing.MonitorDrawing.measureTextHeight(scale);
        return new int[]{width, height};
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

        int bufferWidth = getPixelWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int srcIndex = ((dirtyMinY + y) * bufferWidth + (dirtyMinX + x)) * 3;
                int dstIndex = (y * width + x) * 3;
                if (srcIndex + 2 < pixels.length) {
                    dirtyData[dstIndex] = pixels[srcIndex];
                    dirtyData[dstIndex + 1] = pixels[srcIndex + 1];
                    dirtyData[dstIndex + 2] = pixels[srcIndex + 2];
                }
            }
        }

        MonitorUpdatePacket packet = new MonitorUpdatePacket(worldPosition, dirtyMinX, dirtyMinY, dirtyMaxX, dirtyMaxY, dirtyData);
        ModNetworking.sendToAllTracking((ServerLevel) level, worldPosition, packet);

        dirtyMinX = Integer.MAX_VALUE;
        dirtyMinY = Integer.MAX_VALUE;
        dirtyMaxX = -1;
        dirtyMaxY = -1;
        lastUpdateTime = System.currentTimeMillis();
    }

    public void applyUpdate(int minX, int minY, int maxX, int maxY, byte[] data) {
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        int bufferWidth = getPixelWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int srcIndex = (y * width + x) * 3;
                int dstIndex = ((minY + y) * bufferWidth + (minX + x)) * 3;

                if (dstIndex + 2 >= pixels.length) {
                    return;
                }

                pixels[dstIndex] = data[srcIndex];
                pixels[dstIndex + 1] = data[srcIndex + 1];
                pixels[dstIndex + 2] = data[srcIndex + 2];
            }
        }
    }

    public void tryFormStructure() {
        if (level == null || level.isClientSide) {
            return;
        }

        Set<BlockPos> adjacentMonitors = findAdjacentMonitors(worldPosition);
        if (adjacentMonitors.isEmpty()) {
            return;
        }

        adjacentMonitors.add(worldPosition);

        int minX = worldPosition.getX();
        int maxX = worldPosition.getX();
        int minY = worldPosition.getY();
        int maxY = worldPosition.getY();
        int minZ = worldPosition.getZ();
        int maxZ = worldPosition.getZ();

        for (BlockPos pos : adjacentMonitors) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        int xSize = maxX - minX + 1;
        int ySize = maxY - minY + 1;
        int zSize = maxZ - minZ + 1;

        int flatDimensions = 0;
        if (xSize == 1) flatDimensions++;
        if (ySize == 1) flatDimensions++;
        if (zSize == 1) flatDimensions++;

        if (flatDimensions != 1) {
            return;
        }

        if (!isRectangle(minX, minY, minZ, xSize, ySize, zSize)) {
            return;
        }

        int structureWidth, structureHeight;
        BlockPos controllerPos;

        Direction facing = null;
        for (BlockPos pos : adjacentMonitors) {
            if (level.getBlockState(pos).getBlock() instanceof uk.co.techarchitect.wasmcraft.block.MonitorBlock) {
                facing = level.getBlockState(pos).getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING);
                break;
            }
        }

        if (xSize == 1) {
            structureWidth = zSize;
            structureHeight = ySize;
            if (facing == Direction.EAST) {
                controllerPos = new BlockPos(minX, maxY, maxZ);
            } else {
                controllerPos = new BlockPos(minX, maxY, minZ);
            }
        } else if (zSize == 1) {
            structureWidth = xSize;
            structureHeight = ySize;
            if (facing == Direction.SOUTH) {
                controllerPos = new BlockPos(minX, maxY, minZ);
            } else {
                controllerPos = new BlockPos(maxX, maxY, minZ);
            }
        } else {
            structureWidth = xSize;
            structureHeight = zSize;
            controllerPos = new BlockPos(maxX, minY, maxZ);
        }

        formStructure(controllerPos, structureWidth, structureHeight, adjacentMonitors, facing);
    }

    private Set<BlockPos> findAdjacentMonitors(BlockPos start) {
        Set<BlockPos> found = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        toCheck.add(start);
        found.add(start);

        UUID ownerUUID = getOwner();

        while (!toCheck.isEmpty()) {
            BlockPos current = toCheck.poll();

            for (Direction dir : Direction.values()) {
                BlockPos adjacent = current.relative(dir);
                if (found.contains(adjacent)) {
                    continue;
                }

                if (level.getBlockEntity(adjacent) instanceof MonitorBlockEntity monitor) {
                    if (ownerUUID == null || ownerUUID.equals(monitor.getOwner())) {
                        found.add(adjacent);
                        toCheck.add(adjacent);
                    }
                }
            }
        }

        found.remove(start);
        return found;
    }

    private boolean isRectangle(int minX, int minY, int minZ, int xSize, int ySize, int zSize) {
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                for (int z = 0; z < zSize; z++) {
                    BlockPos pos = new BlockPos(minX + x, minY + y, minZ + z);
                    if (!(level.getBlockEntity(pos) instanceof MonitorBlockEntity)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void formStructure(BlockPos controllerPos, int width, int height, Set<BlockPos> allBlocks, Direction facing) {
        MonitorBlockEntity controller = null;

        if (level.getBlockEntity(controllerPos) instanceof MonitorBlockEntity mon) {
            controller = mon;
        }

        if (controller == null) {
            return;
        }

        int pixelWidth = width * resolution;
        int pixelHeight = height * resolution;
        byte[] newBuffer = new byte[pixelWidth * pixelHeight * 3];

        controller.isController = true;
        controller.controllerPos = null;
        controller.structureOrigin = controllerPos;
        controller.structureWidth = width;
        controller.structureHeight = height;
        controller.pixels = newBuffer;
        controller.structureBlocks = new HashSet<>(allBlocks);

        String planeType;
        if (width == controller.structureBlocks.stream().mapToInt(p -> p.getX()).distinct().count()) {
            if (height == controller.structureBlocks.stream().mapToInt(p -> p.getY()).distinct().count()) {
                planeType = (facing == Direction.SOUTH) ? "XY_SOUTH" : "XY_NORTH";
            } else {
                planeType = "XZ";
            }
        } else {
            planeType = (facing == Direction.EAST) ? "YZ_EAST" : "YZ_WEST";
        }

        controller.calculateGridPosition(controllerPos, width, height, planeType);

        for (BlockPos pos : allBlocks) {
            if (!pos.equals(controllerPos)) {
                if (level.getBlockEntity(pos) instanceof MonitorBlockEntity member) {
                    member.isController = false;
                    member.controllerPos = controllerPos;
                    member.structureOrigin = controllerPos;
                    member.structureWidth = width;
                    member.structureHeight = height;
                    member.resolution = controller.resolution;
                    member.pixels = null;
                    member.structureBlocks.clear();
                    member.calculateGridPosition(controllerPos, width, height, planeType);
                    member.setChanged();
                    level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                }
            }
        }

        controller.setChanged();
        level.sendBlockUpdated(controllerPos, level.getBlockState(controllerPos), level.getBlockState(controllerPos), 3);
    }

    public void breakStructure() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (isController) {
            for (BlockPos pos : new HashSet<>(structureBlocks)) {
                if (!pos.equals(worldPosition)) {
                    level.destroyBlock(pos, true);
                }
            }
        } else {
            MonitorBlockEntity controller = getController();
            if (controller != null) {
                controller.breakStructure();
            }
        }
    }

    public void setResolution(int newResolution) {
        if (level == null || level.isClientSide) {
            return;
        }

        newResolution = Math.max(MIN_RESOLUTION, Math.min(MAX_RESOLUTION, newResolution));

        MonitorBlockEntity controller = getController();
        if (controller == null) {
            return;
        }

        if (controller.resolution == newResolution) {
            return;
        }

        controller.resolution = newResolution;

        int pixelWidth = controller.structureWidth * newResolution;
        int pixelHeight = controller.structureHeight * newResolution;
        controller.pixels = new byte[pixelWidth * pixelHeight * 3];

        for (BlockPos pos : controller.structureBlocks) {
            if (level.getBlockEntity(pos) instanceof MonitorBlockEntity member) {
                member.resolution = newResolution;
                member.setChanged();

                // Force NBT sync to client
                BlockState state = level.getBlockState(pos);
                level.sendBlockUpdated(pos, state, state, 3);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.getChunkSource().blockChanged(pos);
                }
            }
        }

        controller.setChanged();
        BlockState controllerState = level.getBlockState(controller.worldPosition);
        level.sendBlockUpdated(controller.worldPosition, controllerState, controllerState, 3);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().blockChanged(controller.worldPosition);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("IsController", isController);
        tag.putInt("StructureWidth", structureWidth);
        tag.putInt("StructureHeight", structureHeight);
        tag.putInt("GridX", gridX);
        tag.putInt("GridY", gridY);
        tag.putInt("Resolution", resolution);

        if (controllerPos != null) {
            tag.putIntArray("ControllerPos", new int[]{controllerPos.getX(), controllerPos.getY(), controllerPos.getZ()});
        }

        if (structureOrigin != null) {
            tag.putIntArray("StructureOrigin", new int[]{structureOrigin.getX(), structureOrigin.getY(), structureOrigin.getZ()});
        }

        if (isController && !structureBlocks.isEmpty()) {
            ListTag blocksList = new ListTag();
            for (BlockPos pos : structureBlocks) {
                blocksList.add(new IntArrayTag(new int[]{pos.getX(), pos.getY(), pos.getZ()}));
            }
            tag.put("StructureBlocks", blocksList);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            loadAdditional(tag, registries);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        isController = tag.getBoolean("IsController");
        structureWidth = tag.getInt("StructureWidth");
        structureHeight = tag.getInt("StructureHeight");
        gridX = tag.getInt("GridX");
        gridY = tag.getInt("GridY");
        resolution = tag.contains("Resolution") ? tag.getInt("Resolution") : DEFAULT_RESOLUTION;

        if (tag.contains("ControllerPos", Tag.TAG_INT_ARRAY)) {
            int[] pos = tag.getIntArray("ControllerPos");
            if (pos.length == 3) {
                controllerPos = new BlockPos(pos[0], pos[1], pos[2]);
            }
        }

        if (tag.contains("StructureOrigin", Tag.TAG_INT_ARRAY)) {
            int[] pos = tag.getIntArray("StructureOrigin");
            if (pos.length == 3) {
                structureOrigin = new BlockPos(pos[0], pos[1], pos[2]);
            }
        }

        if (tag.contains("StructureBlocks", Tag.TAG_LIST)) {
            structureBlocks.clear();
            ListTag blocksList = tag.getList("StructureBlocks", Tag.TAG_INT_ARRAY);
            for (int i = 0; i < blocksList.size(); i++) {
                int[] pos = blocksList.getIntArray(i);
                if (pos.length == 3) {
                    structureBlocks.add(new BlockPos(pos[0], pos[1], pos[2]));
                }
            }
        }

        int expectedBufferSize = structureWidth * structureHeight * resolution * resolution * 3;

        if (isController) {
            if (pixels == null || pixels.length != expectedBufferSize) {
                int pixelWidth = structureWidth * resolution;
                int pixelHeight = structureHeight * resolution;
                pixels = new byte[pixelWidth * pixelHeight * 3];
            }
        } else {
            pixels = null;
        }
    }
}

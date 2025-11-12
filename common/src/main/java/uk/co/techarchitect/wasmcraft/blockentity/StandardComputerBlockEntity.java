package uk.co.techarchitect.wasmcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import uk.co.techarchitect.wasmcraft.chunkloading.ChunkLoadingManager;
import uk.co.techarchitect.wasmcraft.computer.ComputerBlockEntityBase;
import uk.co.techarchitect.wasmcraft.computer.command.builtin.*;
import uk.co.techarchitect.wasmcraft.menu.ComputerMenu;
import uk.co.techarchitect.wasmcraft.peripheral.Peripheral;
import uk.co.techarchitect.wasmcraft.peripheral.PeripheralManager;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmErrorHelper;
import uk.co.techarchitect.wasmcraft.wasm.context.MonitorContext;
import uk.co.techarchitect.wasmcraft.wasm.context.PeripheralContext;
import uk.co.techarchitect.wasmcraft.wasm.context.RedstoneContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.*;

public class StandardComputerBlockEntity extends ComputerBlockEntityBase implements RedstoneContext, PeripheralContext, MonitorContext {
    private static final double PERIPHERAL_RANGE = 16.0;

    private final int[] redstoneOutputs = new int[6];
    private final int[] redstoneInputs = new int[6];
    private final Map<String, UUID> connectedPeripherals = new HashMap<>();

    public StandardComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            ChunkLoadingManager.getInstance().registerChunkLoader(getId(), serverLevel, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    public void onBlockBroken() {
        if (level != null && !level.isClientSide) {
            ChunkLoadingManager.getInstance().unregisterChunkLoader(getId());
        }
    }

    @Override
    protected void registerCommands() {
        commandRegistry.register(new HelpCommand(commandRegistry));
        commandRegistry.register(new ClearCommand());
        commandRegistry.register(new LsCommand());
        commandRegistry.register(new RmCommand());
        commandRegistry.register(new DownloadCommand());
        commandRegistry.register(new RunCommand(this));
        commandRegistry.register(new StopCommand());
    }

    @Override
    protected WasmContext[] getContexts() {
        return new WasmContext[] {
            (RedstoneContext) this,
            (PeripheralContext) this,
            (MonitorContext) this
        };
    }

    @Override
    public com.dylibso.chicory.runtime.HostFunction[] toHostFunctions() {
        com.dylibso.chicory.runtime.HostFunction[] redstone = RedstoneContext.super.toHostFunctions();
        com.dylibso.chicory.runtime.HostFunction[] peripheral = PeripheralContext.super.toHostFunctions();
        com.dylibso.chicory.runtime.HostFunction[] monitor = MonitorContext.super.toHostFunctions();

        com.dylibso.chicory.runtime.HostFunction[] combined = new com.dylibso.chicory.runtime.HostFunction[
            redstone.length + peripheral.length + monitor.length];
        System.arraycopy(redstone, 0, combined, 0, redstone.length);
        System.arraycopy(peripheral, 0, combined, redstone.length, peripheral.length);
        System.arraycopy(monitor, 0, combined, redstone.length + peripheral.length, monitor.length);

        return combined;
    }

    private Direction relativeToWorldDirection(int relativeSide) {
        if (level == null) return Direction.NORTH;
        Direction facing = getBlockState().getValue(uk.co.techarchitect.wasmcraft.block.ComputerBlock.FACING);

        return switch (relativeSide) {
            case 0 -> Direction.DOWN;
            case 1 -> Direction.UP;
            case 2 -> facing;
            case 3 -> facing.getOpposite();
            case 4 -> facing.getClockWise();
            case 5 -> facing.getCounterClockWise();
            default -> Direction.NORTH;
        };
    }

    @Override
    public int getRedstoneInput(int relativeSide, int[] outPower) {
        if (relativeSide < 0 || relativeSide > 5) {
            outPower[0] = 0;
            return ERR_REDSTONE_INVALID_SIDE;
        }
        Direction worldDir = relativeToWorldDirection(relativeSide);
        outPower[0] = redstoneInputs[worldDir.get3DDataValue()];
        return SUCCESS;
    }

    public void updateRedstoneInputs() {
        if (level == null || level.isClientSide) return;

        for (Direction direction : Direction.values()) {
            int index = direction.get3DDataValue();
            redstoneInputs[index] = level.getSignal(worldPosition.relative(direction), direction);
        }
    }

    @Override
    public int setRedstoneOutput(int relativeSide, int power) {
        if (relativeSide < 0 || relativeSide > 5) {
            return ERR_REDSTONE_INVALID_SIDE;
        }
        if (power < 0 || power > 15) {
            return ERR_REDSTONE_INVALID_POWER;
        }
        Direction worldDir = relativeToWorldDirection(relativeSide);
        int worldIndex = worldDir.get3DDataValue();
        redstoneOutputs[worldIndex] = power;
        if (level != null && !level.isClientSide) {
            level.getServer().execute(() -> {
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
                setChanged();
            });
        }
        return SUCCESS;
    }

    public int getRedstoneOutput(Direction worldDirection) {
        int index = worldDirection.get3DDataValue();
        if (index >= 0 && index < 6) {
            return redstoneOutputs[index];
        }
        return 0;
    }

    @Override
    public int listPeripherals(StringBuilder outJson) {
        if (owner == null || level == null || level.isClientSide) {
            outJson.append("[]");
            return SUCCESS;
        }

        List<Peripheral> peripherals = PeripheralManager.getInstance()
                .findInRange(worldPosition, PERIPHERAL_RANGE, owner);

        outJson.append("[");
        for (int i = 0; i < peripherals.size(); i++) {
            if (i > 0) outJson.append(",");
            Peripheral p = peripherals.get(i);
            outJson.append("{");
            outJson.append("\"type\":\"").append(p.getPeripheralType()).append("\",");
            outJson.append("\"label\":\"").append(p.getLabel()).append("\"");
            outJson.append("}");
        }
        outJson.append("]");

        return SUCCESS;
    }

    @Override
    public int connectPeripheral(String label, StringBuilder outId) {
        if (owner == null || level == null || level.isClientSide) {
            return ERR_INVALID_PARAMETER;
        }

        if (connectedPeripherals.containsKey(label)) {
            UUID existingUUID = connectedPeripherals.get(label);
            Peripheral existing = PeripheralManager.getInstance().findById(existingUUID);
            if (existing != null) {
                outId.append(label);
                return SUCCESS;
            } else {
                connectedPeripherals.remove(label);
            }
        }

        Peripheral peripheral = PeripheralManager.getInstance().findByLabel(label, owner);
        if (peripheral == null) {
            return ERR_PERIPHERAL_NOT_FOUND;
        }

        double distance = Math.sqrt(worldPosition.distSqr(peripheral.getPosition()));

        if (distance > PERIPHERAL_RANGE) {
            return ERR_PERIPHERAL_OUT_OF_RANGE;
        }

        connectedPeripherals.put(label, peripheral.getId());

        if (level != null && !level.isClientSide) {
            level.getServer().execute(this::setChanged);
        }

        outId.append(label);
        return SUCCESS;
    }

    @Override
    public int disconnectPeripheral(String peripheralId) {
        boolean removed = connectedPeripherals.entrySet().removeIf(entry -> entry.getValue().toString().equals(peripheralId));
        if (!removed) {
            return ERR_PERIPHERAL_NOT_CONNECTED;
        }
        setChanged();
        return SUCCESS;
    }

    @Override
    public int setPixel(String monitorId, int x, int y, int r, int g, int b) {
        if (!WasmErrorHelper.isValidColor(r) || !WasmErrorHelper.isValidColor(g) || !WasmErrorHelper.isValidColor(b)) {
            return ERR_MONITOR_INVALID_COLOR;
        }
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return ERR_MONITOR_NOT_FOUND;
        }
        monitor.setPixel(x, y, r, g, b);
        return SUCCESS;
    }

    @Override
    public int getPixel(String monitorId, int x, int y, int[] outRgb) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            outRgb[0] = 0;
            outRgb[1] = 0;
            outRgb[2] = 0;
            return ERR_MONITOR_NOT_FOUND;
        }
        int[] rgb = monitor.getPixel(x, y);
        outRgb[0] = rgb[0];
        outRgb[1] = rgb[1];
        outRgb[2] = rgb[2];
        return SUCCESS;
    }

    @Override
    public int clear(String monitorId, int r, int g, int b) {
        if (!WasmErrorHelper.isValidColor(r) || !WasmErrorHelper.isValidColor(g) || !WasmErrorHelper.isValidColor(b)) {
            return ERR_MONITOR_INVALID_COLOR;
        }
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return ERR_MONITOR_NOT_FOUND;
        }
        monitor.clear(r, g, b);
        return SUCCESS;
    }

    @Override
    public int getSize(String monitorId, int[] outSize) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            outSize[0] = 0;
            outSize[1] = 0;
            return ERR_MONITOR_NOT_FOUND;
        }
        MonitorBlockEntity controller = monitor.getController();
        if (controller == null) {
            outSize[0] = 0;
            outSize[1] = 0;
            return ERR_MONITOR_DISCONNECTED;
        }
        outSize[0] = controller.getPixelWidth();
        outSize[1] = controller.getPixelHeight();
        return SUCCESS;
    }

    @Override
    public int setResolution(String monitorId, int width, int height) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return ERR_MONITOR_NOT_FOUND;
        }
        if (width != height || width < 1 || width > 256) {
            return ERR_MONITOR_INVALID_RESOLUTION;
        }
        monitor.setResolution(width);
        return SUCCESS;
    }

    @Override
    public int fillRect(String monitorId, int x, int y, int width, int height, int r, int g, int b) {
        if (!WasmErrorHelper.isValidColor(r) || !WasmErrorHelper.isValidColor(g) || !WasmErrorHelper.isValidColor(b)) {
            return ERR_MONITOR_INVALID_COLOR;
        }
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return ERR_MONITOR_NOT_FOUND;
        }
        monitor.fillRect(x, y, width, height, r, g, b);
        return SUCCESS;
    }

    @Override
    public int drawHLine(String monitorId, int x, int y, int length, int r, int g, int b) {
        if (!WasmErrorHelper.isValidColor(r) || !WasmErrorHelper.isValidColor(g) || !WasmErrorHelper.isValidColor(b)) {
            return ERR_MONITOR_INVALID_COLOR;
        }
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return ERR_MONITOR_NOT_FOUND;
        }
        monitor.drawHLine(x, y, length, r, g, b);
        return SUCCESS;
    }

    @Override
    public int drawVLine(String monitorId, int x, int y, int length, int r, int g, int b) {
        if (!WasmErrorHelper.isValidColor(r) || !WasmErrorHelper.isValidColor(g) || !WasmErrorHelper.isValidColor(b)) {
            return ERR_MONITOR_INVALID_COLOR;
        }
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return ERR_MONITOR_NOT_FOUND;
        }
        monitor.drawVLine(x, y, length, r, g, b);
        return SUCCESS;
    }

    @Override
    public int drawRect(String monitorId, int x, int y, int width, int height, int r, int g, int b) {
        if (!WasmErrorHelper.isValidColor(r) || !WasmErrorHelper.isValidColor(g) || !WasmErrorHelper.isValidColor(b)) {
            return ERR_MONITOR_INVALID_COLOR;
        }
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return ERR_MONITOR_NOT_FOUND;
        }
        monitor.drawRect(x, y, width, height, r, g, b);
        return SUCCESS;
    }

    @Override
    public int drawChar(String monitorId, int x, int y, char c, int fgR, int fgG, int fgB, int bgR, int bgG, int bgB, int scale) {
        if (!WasmErrorHelper.isValidColor(fgR) || !WasmErrorHelper.isValidColor(fgG) || !WasmErrorHelper.isValidColor(fgB) ||
            !WasmErrorHelper.isValidColor(bgR) || !WasmErrorHelper.isValidColor(bgG) || !WasmErrorHelper.isValidColor(bgB)) {
            return ERR_MONITOR_INVALID_COLOR;
        }
        if (!WasmErrorHelper.isValidChar(c)) {
            return ERR_MONITOR_INVALID_CHAR;
        }
        if (!WasmErrorHelper.isValidScale(scale)) {
            return ERR_MONITOR_INVALID_SCALE;
        }
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return ERR_MONITOR_NOT_FOUND;
        }
        monitor.drawChar(x, y, c, fgR, fgG, fgB, bgR, bgG, bgB, scale);
        return SUCCESS;
    }

    @Override
    public int drawText(String monitorId, int x, int y, String text, int fgR, int fgG, int fgB, int bgR, int bgG, int bgB, int scale, int[] outWidth) {
        if (!WasmErrorHelper.isValidColor(fgR) || !WasmErrorHelper.isValidColor(fgG) || !WasmErrorHelper.isValidColor(fgB) ||
            !WasmErrorHelper.isValidColor(bgR) || !WasmErrorHelper.isValidColor(bgG) || !WasmErrorHelper.isValidColor(bgB)) {
            outWidth[0] = 0;
            return ERR_MONITOR_INVALID_COLOR;
        }
        if (!WasmErrorHelper.isValidScale(scale)) {
            outWidth[0] = 0;
            return ERR_MONITOR_INVALID_SCALE;
        }
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            outWidth[0] = 0;
            return ERR_MONITOR_NOT_FOUND;
        }
        outWidth[0] = monitor.drawText(x, y, text, fgR, fgG, fgB, bgR, bgG, bgB, scale);
        return SUCCESS;
    }

    @Override
    public int measureText(String monitorId, String text, int scale, int[] outDimensions) {
        if (!WasmErrorHelper.isValidScale(scale)) {
            outDimensions[0] = 0;
            outDimensions[1] = 0;
            return ERR_MONITOR_INVALID_SCALE;
        }
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            outDimensions[0] = 0;
            outDimensions[1] = 0;
            return ERR_MONITOR_NOT_FOUND;
        }
        int[] dimensions = monitor.measureText(text, scale);
        outDimensions[0] = dimensions[0];
        outDimensions[1] = dimensions[1];
        return SUCCESS;
    }

    @Override
    public int copyRegion(String monitorId, int srcX, int srcY, int width, int height, int dstX, int dstY) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return ERR_MONITOR_NOT_FOUND;
        }
        monitor.copyRegion(srcX, srcY, width, height, dstX, dstY);
        return SUCCESS;
    }

    private MonitorBlockEntity getConnectedMonitor(String monitorId) {
        if (level == null || level.isClientSide) {
            return null;
        }

        UUID peripheralUUID = connectedPeripherals.get(monitorId);
        if (peripheralUUID == null) {
            return null;
        }

        Peripheral peripheral = PeripheralManager.getInstance().findById(peripheralUUID);
        if (peripheral == null) {
            connectedPeripherals.remove(monitorId);
            if (level != null && !level.isClientSide) {
                level.getServer().execute(this::setChanged);
            }
            return null;
        }

        if (peripheral instanceof MonitorBlockEntity monitor) {
            return monitor;
        }

        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putIntArray("RedstoneOutputs", redstoneOutputs);
        tag.putIntArray("RedstoneInputs", redstoneInputs);

        CompoundTag peripheralsTag = new CompoundTag();
        for (Map.Entry<String, UUID> entry : connectedPeripherals.entrySet()) {
            peripheralsTag.putUUID(entry.getKey(), entry.getValue());
        }
        tag.put("ConnectedPeripherals", peripheralsTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("RedstoneOutputs", Tag.TAG_INT_ARRAY)) {
            int[] loaded = tag.getIntArray("RedstoneOutputs");
            System.arraycopy(loaded, 0, redstoneOutputs, 0, Math.min(loaded.length, redstoneOutputs.length));
        }

        if (tag.contains("RedstoneInputs", Tag.TAG_INT_ARRAY)) {
            int[] loaded = tag.getIntArray("RedstoneInputs");
            System.arraycopy(loaded, 0, redstoneInputs, 0, Math.min(loaded.length, redstoneInputs.length));
        }

        connectedPeripherals.clear();
        if (tag.contains("ConnectedPeripherals", Tag.TAG_COMPOUND)) {
            CompoundTag peripheralsTag = tag.getCompound("ConnectedPeripherals");
            for (String key : peripheralsTag.getAllKeys()) {
                connectedPeripherals.put(key, peripheralsTag.getUUID(key));
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Computer");
    }

    @Override
    public void prepareForExecution() {
        updateRedstoneInputs();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ComputerMenu(containerId, playerInventory, this);
    }
}

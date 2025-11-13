package uk.co.techarchitect.wasmcraft.wasm.context;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import uk.co.techarchitect.wasmcraft.blockentity.MonitorBlockEntity;
import uk.co.techarchitect.wasmcraft.peripheral.Peripheral;
import uk.co.techarchitect.wasmcraft.peripheral.PeripheralManager;
import uk.co.techarchitect.wasmcraft.wasm.WasmErrorHelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.*;

public class ContextHelper implements RedstoneContext, PeripheralContext, MonitorContext {
    private final Map<String, UUID> connectedPeripherals;
    private final int[] redstoneOutputs;
    private final int[] redstoneInputs;
    private final PeripheralProvider peripheralProvider;
    private final LevelProvider levelProvider;

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

    public interface PeripheralProvider {
        BlockPos getPosition();
        UUID getOwner();
        double getPeripheralRange();
    }

    public interface LevelProvider {
        ServerLevel getLevel();
    }

    public ContextHelper(Map<String, UUID> connectedPeripherals, int[] redstoneOutputs, int[] redstoneInputs,
                        PeripheralProvider peripheralProvider, LevelProvider levelProvider) {
        this.connectedPeripherals = connectedPeripherals;
        this.redstoneOutputs = redstoneOutputs;
        this.redstoneInputs = redstoneInputs;
        this.peripheralProvider = peripheralProvider;
        this.levelProvider = levelProvider;
    }

    public int getRedstoneInput(int relativeSide, int[] outPower) {
        if (relativeSide < 0 || relativeSide >= 6) {
            outPower[0] = 0;
            return ERR_INVALID_PARAMETER;
        }
        outPower[0] = redstoneInputs[relativeSide];
        return SUCCESS;
    }

    public int setRedstoneOutput(int relativeSide, int power) {
        if (relativeSide < 0 || relativeSide >= 6) {
            return ERR_INVALID_PARAMETER;
        }
        if (power < 0 || power > 15) {
            return ERR_INVALID_PARAMETER;
        }
        redstoneOutputs[relativeSide] = power;
        return SUCCESS;
    }

    public int listPeripherals(StringBuilder outJson) {
        UUID owner = peripheralProvider.getOwner();
        ServerLevel level = levelProvider.getLevel();

        if (owner == null || level == null || level.isClientSide) {
            outJson.append("[]");
            return SUCCESS;
        }

        List<Peripheral> peripherals = PeripheralManager.getInstance()
                .findInRange(peripheralProvider.getPosition(), peripheralProvider.getPeripheralRange(), owner);

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

    public int connectPeripheral(String label, StringBuilder outId) {
        UUID owner = peripheralProvider.getOwner();
        ServerLevel level = levelProvider.getLevel();

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

        BlockPos pos = peripheralProvider.getPosition();
        double distance = Math.sqrt(pos.distSqr(peripheral.getPosition()));

        if (distance > peripheralProvider.getPeripheralRange()) {
            return ERR_PERIPHERAL_OUT_OF_RANGE;
        }

        connectedPeripherals.put(label, peripheral.getId());
        outId.append(label);
        return SUCCESS;
    }

    public int disconnectPeripheral(String peripheralId) {
        boolean removed = connectedPeripherals.entrySet().removeIf(entry -> entry.getValue().toString().equals(peripheralId));
        if (!removed) {
            return ERR_PERIPHERAL_NOT_CONNECTED;
        }
        return SUCCESS;
    }

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

    public int copyRegion(String monitorId, int srcX, int srcY, int width, int height, int dstX, int dstY) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return ERR_MONITOR_NOT_FOUND;
        }
        monitor.copyRegion(srcX, srcY, width, height, dstX, dstY);
        return SUCCESS;
    }

    private MonitorBlockEntity getConnectedMonitor(String monitorId) {
        ServerLevel level = levelProvider.getLevel();
        if (level == null || level.isClientSide) {
            return null;
        }

        UUID peripheralUUID = connectedPeripherals.get(monitorId);
        if (peripheralUUID == null) {
            return null;
        }

        Peripheral peripheral = PeripheralManager.getInstance().findById(peripheralUUID);
        if (peripheral == null) {
            connectedPeripherals.remove(peripheralUUID);
            return null;
        }

        if (peripheral instanceof MonitorBlockEntity monitor) {
            return monitor;
        }

        return null;
    }
}

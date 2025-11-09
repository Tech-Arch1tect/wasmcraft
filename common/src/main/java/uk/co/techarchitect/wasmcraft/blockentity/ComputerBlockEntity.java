package uk.co.techarchitect.wasmcraft.blockentity;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.techarchitect.wasmcraft.menu.ComputerMenu;
import uk.co.techarchitect.wasmcraft.network.ComputerOutputSyncPacket;
import uk.co.techarchitect.wasmcraft.peripheral.Peripheral;
import uk.co.techarchitect.wasmcraft.peripheral.PeripheralManager;
import uk.co.techarchitect.wasmcraft.wasm.WasmExecutor;
import uk.co.techarchitect.wasmcraft.wasm.context.MonitorContext;
import uk.co.techarchitect.wasmcraft.wasm.context.PeripheralContext;
import uk.co.techarchitect.wasmcraft.wasm.context.RedstoneContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ComputerBlockEntity extends BlockEntity implements ExtendedMenuProvider, RedstoneContext, PeripheralContext, MonitorContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComputerBlockEntity.class);
    private static final int MAX_HISTORY_LINES = 100;
    private static final double PERIPHERAL_RANGE = 16.0;

    private final List<String> outputHistory = new ArrayList<>();
    private final Map<String, byte[]> fileSystem = new HashMap<>();
    private final int[] redstoneOutputs = new int[6];
    private final int[] redstoneInputs = new int[6];
    private final Map<String, UUID> connectedPeripherals = new HashMap<>();
    private final List<ServerPlayer> activeViewers = new ArrayList<>();
    private UUID owner;
    private WasmExecutor.ExecutionHandle activeExecution;

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(), pos, state);
        addOutputLine("Computer initialized. Type 'help' for commands.");
    }

    private void addOutputLine(String line) {
        outputHistory.add(line);
        while (outputHistory.size() > MAX_HISTORY_LINES) {
            outputHistory.remove(0);
        }
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
    public int getRedstoneInput(int relativeSide) {
        if (relativeSide < 0 || relativeSide > 5) {
            return 0;
        }
        Direction worldDir = relativeToWorldDirection(relativeSide);
        return redstoneInputs[worldDir.get3DDataValue()];
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
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

    public void updateRedstoneInputs() {
        if (level == null || level.isClientSide) return;

        for (Direction direction : Direction.values()) {
            int index = direction.get3DDataValue();
            redstoneInputs[index] = level.getSignal(worldPosition.relative(direction), direction);
        }
    }

    @Override
    public void setRedstoneOutput(int relativeSide, int power) {
        if (relativeSide >= 0 && relativeSide <= 5) {
            Direction worldDir = relativeToWorldDirection(relativeSide);
            int worldIndex = worldDir.get3DDataValue();
            redstoneOutputs[worldIndex] = Math.max(0, Math.min(15, power));
            if (level != null && !level.isClientSide) {
                level.getServer().execute(() -> {
                    level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
                    setChanged();
                });
            }
        }
    }

    public int getRedstoneOutput(Direction worldDirection) {
        int index = worldDirection.get3DDataValue();
        if (index >= 0 && index < 6) {
            return redstoneOutputs[index];
        }
        return 0;
    }

    @Override
    public String listPeripherals() {
        if (owner == null || level == null || level.isClientSide) {
            return "[]";
        }

        List<Peripheral> peripherals = PeripheralManager.getInstance()
                .findInRange(worldPosition, PERIPHERAL_RANGE, owner);

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < peripherals.size(); i++) {
            if (i > 0) json.append(",");
            Peripheral p = peripherals.get(i);
            json.append("{");
            json.append("\"type\":\"").append(p.getPeripheralType()).append("\",");
            json.append("\"label\":\"").append(p.getLabel()).append("\"");
            json.append("}");
        }
        json.append("]");

        return json.toString();
    }

    @Override
    public String connectPeripheral(String label) {
        if (owner == null || level == null || level.isClientSide) {
            return "ERROR: Not initialized";
        }

        if (connectedPeripherals.containsKey(label)) {
            UUID existingUUID = connectedPeripherals.get(label);
            Peripheral existing = PeripheralManager.getInstance().findById(existingUUID);
            if (existing != null) {
                return "ERROR: Already connected to " + label;
            } else {
                connectedPeripherals.remove(label);
            }
        }

        Peripheral peripheral = PeripheralManager.getInstance().findByLabel(label, owner);
        if (peripheral == null) {
            return "ERROR: Peripheral not found: " + label;
        }

        double distance = Math.sqrt(worldPosition.distSqr(peripheral.getPosition()));

        if (distance > PERIPHERAL_RANGE) {
            return "ERROR: Peripheral out of range";
        }

        connectedPeripherals.put(label, peripheral.getId());

        if (level != null && !level.isClientSide) {
            level.getServer().execute(this::setChanged);
        }

        String result = "Connected to " + label + " (" + peripheral.getPeripheralType() + ")";
        return result;
    }

    @Override
    public void disconnectPeripheral(String peripheralId) {
        connectedPeripherals.entrySet().removeIf(entry -> entry.getValue().toString().equals(peripheralId));
        setChanged();
    }

    @Override
    public void setPixel(String monitorId, int x, int y, int r, int g, int b) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return;
        }
        monitor.setPixel(x, y, r, g, b);
    }

    @Override
    public int[] getPixel(String monitorId, int x, int y) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return new int[]{0, 0, 0};
        }
        return monitor.getPixel(x, y);
    }

    @Override
    public void clear(String monitorId, int r, int g, int b) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return;
        }
        monitor.clear(r, g, b);
    }

    @Override
    public int[] getSize(String monitorId) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return new int[]{0, 0};
        }
        MonitorBlockEntity controller = monitor.getController();
        if (controller == null) {
            return new int[]{0, 0};
        }
        return new int[]{controller.getPixelWidth(), controller.getPixelHeight()};
    }

    @Override
    public void setResolution(String monitorId, int width, int height) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return;
        }
        if (width == height) {
            monitor.setResolution(width);
        }
    }

    @Override
    public void fillRect(String monitorId, int x, int y, int width, int height, int r, int g, int b) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return;
        }
        monitor.fillRect(x, y, width, height, r, g, b);
    }

    @Override
    public void drawHLine(String monitorId, int x, int y, int length, int r, int g, int b) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return;
        }
        monitor.drawHLine(x, y, length, r, g, b);
    }

    @Override
    public void drawVLine(String monitorId, int x, int y, int length, int r, int g, int b) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return;
        }
        monitor.drawVLine(x, y, length, r, g, b);
    }

    @Override
    public void drawRect(String monitorId, int x, int y, int width, int height, int r, int g, int b) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return;
        }
        monitor.drawRect(x, y, width, height, r, g, b);
    }

    @Override
    public void drawChar(String monitorId, int x, int y, char c, int fgR, int fgG, int fgB, int bgR, int bgG, int bgB, int scale) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return;
        }
        monitor.drawChar(x, y, c, fgR, fgG, fgB, bgR, bgG, bgB, scale);
    }

    @Override
    public int drawText(String monitorId, int x, int y, String text, int fgR, int fgG, int fgB, int bgR, int bgG, int bgB, int scale) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return 0;
        }
        return monitor.drawText(x, y, text, fgR, fgG, fgB, bgR, bgG, bgB, scale);
    }

    @Override
    public int[] measureText(String monitorId, String text, int scale) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return new int[]{0, 0};
        }
        return monitor.measureText(text, scale);
    }

    @Override
    public void copyRegion(String monitorId, int srcX, int srcY, int width, int height, int dstX, int dstY) {
        MonitorBlockEntity monitor = getConnectedMonitor(monitorId);
        if (monitor == null) {
            return;
        }
        monitor.copyRegion(srcX, srcY, width, height, dstX, dstY);
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

    public List<String> getOutputHistory() {
        return new ArrayList<>(outputHistory);
    }

    public void tick() {
        if (activeExecution != null) {
            List<String> stdoutLines = activeExecution.getStdout().pollLines();
            List<String> stderrLines = activeExecution.getStderr().pollLines();

            boolean hasOutput = false;

            for (String line : stdoutLines) {
                addOutputLine(line);
                hasOutput = true;
            }

            for (String line : stderrLines) {
                addOutputLine("STDERR: " + line);
                hasOutput = true;
            }

            WasmExecutor.ExecutionResult result = activeExecution.getResultIfDone();
            if (result != null) {
                List<String> finalStdout = activeExecution.getStdout().pollLines();
                List<String> finalStderr = activeExecution.getStderr().pollLines();

                for (String line : finalStdout) {
                    addOutputLine(line);
                    hasOutput = true;
                }

                for (String line : finalStderr) {
                    addOutputLine("STDERR: " + line);
                    hasOutput = true;
                }

                String remainingOut = activeExecution.getStdout().flushRemaining();
                if (remainingOut != null && !remainingOut.isEmpty()) {
                    addOutputLine(remainingOut);
                    hasOutput = true;
                }

                String remainingErr = activeExecution.getStderr().flushRemaining();
                if (remainingErr != null && !remainingErr.isEmpty()) {
                    addOutputLine("STDERR: " + remainingErr);
                    hasOutput = true;
                }

                if (!result.isSuccess()) {
                    addOutputLine("ERROR: " + result.getError());
                    hasOutput = true;
                }
                activeExecution = null;
                hasOutput = true;
            }

            if (hasOutput) {
                setChanged();
                syncToAllViewers();
            }
        }
    }

    public void executeCommand(String command) {
        addOutputLine("> " + command);

        String[] parts = command.toLowerCase().trim().split("\\s+");
        String cmd = parts[0];

        switch (cmd) {
            case "help" -> {
                addOutputLine("Available commands:");
                addOutputLine("  help            - Show this help message");
                addOutputLine("  clear           - Clear the terminal");
                addOutputLine("  ls              - List files in storage");
                addOutputLine("  rm <file>       - Delete file from storage");
                addOutputLine("  download <url>  - Download WASM from URL");
                addOutputLine("  run <file>      - Execute WASM file");
                addOutputLine("  stop            - Cancel running program");
            }
            case "clear" -> {
                outputHistory.clear();
            }
            case "ls" -> {
                if (fileSystem.isEmpty()) {
                    addOutputLine("No files stored");
                } else {
                    addOutputLine("Files:");
                    for (Map.Entry<String, byte[]> entry : fileSystem.entrySet()) {
                        String name = entry.getKey();
                        int size = entry.getValue().length;
                        addOutputLine(String.format("  %-20s %6d bytes", name, size));
                    }
                }
            }
            case "rm" -> {
                if (parts.length < 2) {
                    addOutputLine("Usage: rm <filename>");
                } else {
                    String filename = parts[1];
                    if (!filename.endsWith(".wasm")) {
                        filename = filename + ".wasm";
                    }

                    if (fileSystem.containsKey(filename)) {
                        fileSystem.remove(filename);
                        addOutputLine("Deleted " + filename);
                    } else {
                        addOutputLine("File not found: " + filename);
                    }
                }
            }
            case "download" -> {
                if (parts.length < 2) {
                    addOutputLine("Usage: download <url>");
                    addOutputLine("Example: download https://example.com/program.wasm");
                } else {
                    String originalCommand = command.trim();
                    int urlStart = originalCommand.toLowerCase().indexOf("download") + "download".length();
                    String url = originalCommand.substring(urlStart).trim();

                    String filename;
                    try {
                        String path = url.substring(url.lastIndexOf('/') + 1);
                        if (path.isEmpty() || !path.contains(".")) {
                            addOutputLine("ERROR: Invalid URL - cannot determine filename");
                            break;
                        }
                        filename = path;
                        if (!filename.endsWith(".wasm")) {
                            filename = filename + ".wasm";
                        }
                    } catch (Exception e) {
                        addOutputLine("ERROR: Invalid URL format");
                        break;
                    }

                    addOutputLine("Downloading from " + url + "...");

                    try {
                        byte[] data = downloadFile(url);
                        fileSystem.put(filename, data);
                        addOutputLine("Downloaded " + filename + " (" + data.length + " bytes)");
                        setChanged();
                    } catch (Exception e) {
                        addOutputLine("ERROR: " + e.getMessage());
                    }
                }
            }
            case "run" -> {
                if (parts.length < 2) {
                    addOutputLine("Usage: run <filename>");
                    addOutputLine("Example: run hello.wasm");
                } else {
                    if (activeExecution != null && !activeExecution.isDone()) {
                        addOutputLine("ERROR: A program is already running. Use 'stop' to cancel it.");
                    } else {
                        String filename = parts[1];
                        if (!filename.endsWith(".wasm")) {
                            filename = filename + ".wasm";
                        }

                        addOutputLine("Executing " + filename + "...");

                        updateRedstoneInputs();

                        byte[] wasmBytes;
                        if (fileSystem.containsKey(filename)) {
                            wasmBytes = fileSystem.get(filename);
                        } else {
                            addOutputLine("File not found: " + filename);
                            break;
                        }

                        activeExecution = WasmExecutor.executeAsync(wasmBytes, this);
                    }
                }
            }
            case "stop" -> {
                if (activeExecution == null || activeExecution.isDone()) {
                    addOutputLine("No program is currently running");
                } else {
                    if (activeExecution.cancel()) {
                        addOutputLine("Program cancelled");
                        activeExecution = null;
                    } else {
                        addOutputLine("Failed to cancel program");
                    }
                }
            }
            default -> {
                addOutputLine("Unknown command: " + cmd);
                addOutputLine("Type 'help' for available commands");
            }
        }

        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        ListTag historyList = new ListTag();
        for (String line : outputHistory) {
            historyList.add(StringTag.valueOf(line));
        }
        tag.put("OutputHistory", historyList);

        CompoundTag filesTag = new CompoundTag();
        for (Map.Entry<String, byte[]> entry : fileSystem.entrySet()) {
            filesTag.putByteArray(entry.getKey(), entry.getValue());
        }
        tag.put("FileSystem", filesTag);

        tag.putIntArray("RedstoneOutputs", redstoneOutputs);
        tag.putIntArray("RedstoneInputs", redstoneInputs);

        if (owner != null) {
            tag.putUUID("Owner", owner);
        }

        CompoundTag peripheralsTag = new CompoundTag();
        for (Map.Entry<String, UUID> entry : connectedPeripherals.entrySet()) {
            peripheralsTag.putUUID(entry.getKey(), entry.getValue());
        }
        tag.put("ConnectedPeripherals", peripheralsTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        outputHistory.clear();
        if (tag.contains("OutputHistory", Tag.TAG_LIST)) {
            ListTag historyList = tag.getList("OutputHistory", Tag.TAG_STRING);
            for (int i = 0; i < historyList.size(); i++) {
                addOutputLine(historyList.getString(i));
            }
        }

        fileSystem.clear();
        if (tag.contains("FileSystem", Tag.TAG_COMPOUND)) {
            CompoundTag filesTag = tag.getCompound("FileSystem");
            for (String key : filesTag.getAllKeys()) {
                fileSystem.put(key, filesTag.getByteArray(key));
            }
        }

        if (tag.contains("RedstoneOutputs", Tag.TAG_INT_ARRAY)) {
            int[] loaded = tag.getIntArray("RedstoneOutputs");
            System.arraycopy(loaded, 0, redstoneOutputs, 0, Math.min(loaded.length, redstoneOutputs.length));
        }

        if (tag.contains("RedstoneInputs", Tag.TAG_INT_ARRAY)) {
            int[] loaded = tag.getIntArray("RedstoneInputs");
            System.arraycopy(loaded, 0, redstoneInputs, 0, Math.min(loaded.length, redstoneInputs.length));
        }

        if (tag.hasUUID("Owner")) {
            this.owner = tag.getUUID("Owner");
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Computer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ComputerMenu(containerId, playerInventory, this);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }

    public void addViewer(ServerPlayer player) {
        if (!activeViewers.contains(player)) {
            activeViewers.add(player);
        }
        syncToPlayer(player);
    }

    public void removeViewer(ServerPlayer player) {
        activeViewers.remove(player);
    }

    public void syncToPlayer(ServerPlayer player) {
        NetworkManager.sendToPlayer(player, new ComputerOutputSyncPacket(worldPosition, getOutputHistory()));
    }

    private void syncToAllViewers() {
        activeViewers.removeIf(player -> !player.containerMenu.stillValid(player));
        for (ServerPlayer player : activeViewers) {
            syncToPlayer(player);
        }
    }

    private byte[] downloadFile(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "Wasmcraft/1.0");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage());
        }

        int contentLength = connection.getContentLength();
        if (contentLength > 10 * 1024 * 1024) {
            throw new IOException("File too large (max 10MB)");
        }

        try (InputStream in = connection.getInputStream()) {
            return in.readAllBytes();
        } finally {
            connection.disconnect();
        }
    }
}

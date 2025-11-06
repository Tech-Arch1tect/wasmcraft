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
import uk.co.techarchitect.wasmcraft.menu.ComputerMenu;
import uk.co.techarchitect.wasmcraft.network.ComputerOutputSyncPacket;
import uk.co.techarchitect.wasmcraft.wasm.WasmExecutor;
import uk.co.techarchitect.wasmcraft.wasm.context.RedstoneContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputerBlockEntity extends BlockEntity implements ExtendedMenuProvider, RedstoneContext {
    private final List<String> outputHistory = new ArrayList<>();
    private final Map<String, byte[]> fileSystem = new HashMap<>();
    private final int[] redstoneOutputs = new int[6];
    private final int[] redstoneInputs = new int[6];

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(), pos, state);
        outputHistory.add("Computer initialized. Type 'help' for commands.");
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

    public List<String> getOutputHistory() {
        return new ArrayList<>(outputHistory);
    }

    public void executeCommand(String command) {
        outputHistory.add("> " + command);

        String[] parts = command.toLowerCase().trim().split("\\s+");
        String cmd = parts[0];

        switch (cmd) {
            case "help" -> {
                outputHistory.add("Available commands:");
                outputHistory.add("  help            - Show this help message");
                outputHistory.add("  clear           - Clear the terminal");
                outputHistory.add("  ls              - List files in storage");
                outputHistory.add("  rm <file>       - Delete file from storage");
                outputHistory.add("  download <url>  - Download WASM from URL");
                outputHistory.add("  run <file>      - Execute WASM file");
            }
            case "clear" -> {
                outputHistory.clear();
            }
            case "ls" -> {
                if (fileSystem.isEmpty()) {
                    outputHistory.add("No files stored");
                } else {
                    outputHistory.add("Files:");
                    for (Map.Entry<String, byte[]> entry : fileSystem.entrySet()) {
                        String name = entry.getKey();
                        int size = entry.getValue().length;
                        outputHistory.add(String.format("  %-20s %6d bytes", name, size));
                    }
                }
            }
            case "rm" -> {
                if (parts.length < 2) {
                    outputHistory.add("Usage: rm <filename>");
                } else {
                    String filename = parts[1];
                    if (!filename.endsWith(".wasm")) {
                        filename = filename + ".wasm";
                    }

                    if (fileSystem.containsKey(filename)) {
                        fileSystem.remove(filename);
                        outputHistory.add("Deleted " + filename);
                    } else {
                        outputHistory.add("File not found: " + filename);
                    }
                }
            }
            case "download" -> {
                if (parts.length < 2) {
                    outputHistory.add("Usage: download <url>");
                    outputHistory.add("Example: download https://example.com/program.wasm");
                } else {
                    String originalCommand = command.trim();
                    int urlStart = originalCommand.toLowerCase().indexOf("download") + "download".length();
                    String url = originalCommand.substring(urlStart).trim();

                    String filename;
                    try {
                        String path = url.substring(url.lastIndexOf('/') + 1);
                        if (path.isEmpty() || !path.contains(".")) {
                            outputHistory.add("ERROR: Invalid URL - cannot determine filename");
                            break;
                        }
                        filename = path;
                        if (!filename.endsWith(".wasm")) {
                            filename = filename + ".wasm";
                        }
                    } catch (Exception e) {
                        outputHistory.add("ERROR: Invalid URL format");
                        break;
                    }

                    outputHistory.add("Downloading from " + url + "...");

                    try {
                        byte[] data = downloadFile(url);
                        fileSystem.put(filename, data);
                        outputHistory.add("Downloaded " + filename + " (" + data.length + " bytes)");
                        setChanged();
                    } catch (Exception e) {
                        outputHistory.add("ERROR: " + e.getMessage());
                    }
                }
            }
            case "run" -> {
                if (parts.length < 2) {
                    outputHistory.add("Usage: run <filename>");
                    outputHistory.add("Example: run hello.wasm");
                } else {
                    String filename = parts[1];
                    if (!filename.endsWith(".wasm")) {
                        filename = filename + ".wasm";
                    }

                    outputHistory.add("Executing " + filename + "...");

                    updateRedstoneInputs();

                    WasmExecutor.ExecutionResult result;

                    if (fileSystem.containsKey(filename)) {
                        byte[] wasmBytes = fileSystem.get(filename);
                        result = WasmExecutor.execute(wasmBytes, this);
                    } else {
                        String resourcePath = "/wasm/" + filename;
                        result = WasmExecutor.executeFromResource(resourcePath);
                    }

                    if (result.isSuccess()) {
                        for (String line : result.getOutput().split("\n")) {
                            outputHistory.add(line);
                        }
                    } else {
                        outputHistory.add("ERROR: " + result.getError());
                    }
                }
            }
            default -> {
                outputHistory.add("Unknown command: " + cmd);
                outputHistory.add("Type 'help' for available commands");
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        outputHistory.clear();
        if (tag.contains("OutputHistory", Tag.TAG_LIST)) {
            ListTag historyList = tag.getList("OutputHistory", Tag.TAG_STRING);
            for (int i = 0; i < historyList.size(); i++) {
                outputHistory.add(historyList.getString(i));
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

    public void syncToPlayer(ServerPlayer player) {
        NetworkManager.sendToPlayer(player, new ComputerOutputSyncPacket(worldPosition, getOutputHistory()));
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

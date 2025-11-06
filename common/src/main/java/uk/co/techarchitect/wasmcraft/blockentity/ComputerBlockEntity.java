package uk.co.techarchitect.wasmcraft.blockentity;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputerBlockEntity extends BlockEntity implements ExtendedMenuProvider {
    private final List<String> outputHistory = new ArrayList<>();
    private final Map<String, byte[]> fileSystem = new HashMap<>();

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(), pos, state);
        outputHistory.add("Computer initialized. Type 'help' for commands.");
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

                    WasmExecutor.ExecutionResult result;

                    if (fileSystem.containsKey(filename)) {
                        byte[] wasmBytes = fileSystem.get(filename);
                        result = WasmExecutor.execute(wasmBytes);
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

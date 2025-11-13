package uk.co.techarchitect.wasmcraft.computer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import uk.co.techarchitect.wasmcraft.computer.command.CommandRegistry;
import uk.co.techarchitect.wasmcraft.wasm.WasmExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ComputerCore {
    private static final int MAX_HISTORY_LINES = 100;
    private static final int MAX_COMMAND_HISTORY = 50;

    public interface SyncCallback {
        void syncToPlayer(ServerPlayer player, List<String> outputHistory, List<String> commandHistory, List<String> fileNames);
    }

    private final Terminal terminal;
    private final FileSystem fileSystem;
    private final CommandRegistry commandRegistry;
    private final List<String> commandHistory;
    private final List<ServerPlayer> activeViewers;
    private UUID id;
    private UUID owner;
    private WasmExecutor.ExecutionHandle activeExecution;
    private SyncCallback syncCallback;

    public ComputerCore() {
        this.terminal = new Terminal(MAX_HISTORY_LINES);
        this.fileSystem = new FileSystem();
        this.commandRegistry = new CommandRegistry();
        this.commandHistory = new ArrayList<>();
        this.activeViewers = new ArrayList<>();
        this.id = UUID.randomUUID();
    }

    public void setSyncCallback(SyncCallback callback) {
        this.syncCallback = callback;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public List<String> getCommandHistory() {
        return new ArrayList<>(commandHistory);
    }

    public WasmExecutor.ExecutionHandle getActiveExecution() {
        return activeExecution;
    }

    public void setActiveExecution(WasmExecutor.ExecutionHandle handle) {
        this.activeExecution = handle;
    }

    public void addToCommandHistory(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        if (!commandHistory.isEmpty() && commandHistory.get(commandHistory.size() - 1).equals(command)) {
            return;
        }

        commandHistory.add(command);
        while (commandHistory.size() > MAX_COMMAND_HISTORY) {
            commandHistory.remove(0);
        }
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
        if (syncCallback != null) {
            syncCallback.syncToPlayer(player, terminal.getHistory(), new ArrayList<>(commandHistory), fileSystem.listFiles());
        }
    }

    public void syncToAllViewers() {
        activeViewers.removeIf(player -> !player.containerMenu.stillValid(player));
        for (ServerPlayer player : activeViewers) {
            syncToPlayer(player);
        }
    }

    public void tick(Runnable onOutputCallback) {
        if (activeExecution != null) {
            List<String> stdoutLines = activeExecution.getStdout().pollLines();
            List<String> stderrLines = activeExecution.getStderr().pollLines();

            boolean hasOutput = false;

            for (String line : stdoutLines) {
                terminal.addLine(line);
                hasOutput = true;
            }

            for (String line : stderrLines) {
                terminal.addLine("STDERR: " + line);
                hasOutput = true;
            }

            WasmExecutor.ExecutionResult result = activeExecution.getResultIfDone();
            if (result != null) {
                List<String> finalStdout = activeExecution.getStdout().pollLines();
                List<String> finalStderr = activeExecution.getStderr().pollLines();

                for (String line : finalStdout) {
                    terminal.addLine(line);
                    hasOutput = true;
                }

                for (String line : finalStderr) {
                    terminal.addLine("STDERR: " + line);
                    hasOutput = true;
                }

                String remainingOut = activeExecution.getStdout().flushRemaining();
                if (remainingOut != null && !remainingOut.isEmpty()) {
                    terminal.addLine(remainingOut);
                    hasOutput = true;
                }

                String remainingErr = activeExecution.getStderr().flushRemaining();
                if (remainingErr != null && !remainingErr.isEmpty()) {
                    terminal.addLine("STDERR: " + remainingErr);
                    hasOutput = true;
                }

                if (!result.isSuccess()) {
                    terminal.addLine("ERROR: " + result.getError());
                    hasOutput = true;
                }
                activeExecution = null;
                hasOutput = true;
            }

            if (hasOutput && onOutputCallback != null) {
                onOutputCallback.run();
            }
        }
    }

    public void saveToNbt(CompoundTag tag) {
        ListTag historyList = new ListTag();
        terminal.saveToNbt(historyList);
        tag.put("OutputHistory", historyList);

        ListTag commandHistoryList = new ListTag();
        for (String command : commandHistory) {
            commandHistoryList.add(net.minecraft.nbt.StringTag.valueOf(command));
        }
        tag.put("CommandHistory", commandHistoryList);

        CompoundTag filesTag = new CompoundTag();
        fileSystem.saveToNbt(filesTag);
        tag.put("FileSystem", filesTag);

        tag.putUUID("ComputerId", id);

        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
    }

    public void loadFromNbt(CompoundTag tag) {
        if (tag.contains("OutputHistory", Tag.TAG_LIST)) {
            ListTag historyList = tag.getList("OutputHistory", Tag.TAG_STRING);
            terminal.loadFromNbt(historyList);
        }

        if (tag.contains("FileSystem", Tag.TAG_COMPOUND)) {
            CompoundTag filesTag = tag.getCompound("FileSystem");
            fileSystem.loadFromNbt(filesTag);
        }

        commandHistory.clear();
        if (tag.contains("CommandHistory", Tag.TAG_LIST)) {
            ListTag commandHistoryList = tag.getList("CommandHistory", Tag.TAG_STRING);
            for (int i = 0; i < commandHistoryList.size(); i++) {
                commandHistory.add(commandHistoryList.getString(i));
            }
        }

        if (tag.hasUUID("ComputerId")) {
            this.id = tag.getUUID("ComputerId");
        }

        if (tag.hasUUID("Owner")) {
            this.owner = tag.getUUID("Owner");
        }
    }
}

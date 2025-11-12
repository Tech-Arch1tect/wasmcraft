package uk.co.techarchitect.wasmcraft.computer;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.techarchitect.wasmcraft.computer.command.Command;
import uk.co.techarchitect.wasmcraft.computer.command.CommandContext;
import uk.co.techarchitect.wasmcraft.computer.command.CommandRegistry;
import uk.co.techarchitect.wasmcraft.network.ComputerOutputSyncPacket;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmContextComposer;
import uk.co.techarchitect.wasmcraft.wasm.WasmExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class ComputerBlockEntityBase extends BlockEntity implements ExtendedMenuProvider, CommandContext {
    private static final int MAX_HISTORY_LINES = 100;
    private static final int MAX_COMMAND_HISTORY = 50;

    protected final Terminal terminal = new Terminal(MAX_HISTORY_LINES);
    protected final FileSystem fileSystem = new FileSystem();
    protected final CommandRegistry commandRegistry = new CommandRegistry();
    protected final List<String> commandHistory = new ArrayList<>();
    protected final List<ServerPlayer> activeViewers = new ArrayList<>();
    protected UUID id;
    protected UUID owner;
    protected WasmExecutor.ExecutionHandle activeExecution;

    public ComputerBlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.id = UUID.randomUUID();
        registerCommands();
        terminal.addLine("Computer initialized. Type 'help' for commands.");
    }

    public UUID getId() {
        return id;
    }

    protected abstract void registerCommands();

    protected abstract WasmContext[] getContexts();

    protected WasmContext buildWasmContext() {
        WasmContextComposer composer = new WasmContextComposer();
        for (WasmContext context : getContexts()) {
            composer.add(context);
        }
        return composer;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public UUID getOwner() {
        return owner;
    }

    public List<String> getOutputHistory() {
        return terminal.getHistory();
    }

    public List<String> getCommandHistory() {
        return new ArrayList<>(commandHistory);
    }

    public List<String> getFileNames() {
        return fileSystem.listFiles();
    }

    protected void addToCommandHistory(String command) {
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
        setChanged();
    }

    public void tick() {
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

            if (hasOutput) {
                setChanged();
                syncToAllViewers();
            }
        }
    }

    public void executeCommand(String command) {
        terminal.addLine("> " + command);
        addToCommandHistory(command);

        String[] parts = command.trim().split("\\s+");
        if (parts.length == 0) {
            return;
        }

        String cmd = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        Command commandObj = commandRegistry.get(cmd);
        if (commandObj != null) {
            commandObj.execute(this, args);
        } else {
            terminal.addLine("Unknown command: " + cmd);
            terminal.addLine("Type 'help' for available commands");
        }

        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

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

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

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

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
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
        NetworkManager.sendToPlayer(player, new ComputerOutputSyncPacket(worldPosition, getOutputHistory(), getCommandHistory(), getFileNames()));
    }

    protected void syncToAllViewers() {
        activeViewers.removeIf(player -> !player.containerMenu.stillValid(player));
        for (ServerPlayer player : activeViewers) {
            syncToPlayer(player);
        }
    }

    @Override
    public void addOutput(String line) {
        terminal.addLine(line);
    }

    @Override
    public Terminal getTerminal() {
        return terminal;
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public WasmExecutor.ExecutionHandle getActiveExecution() {
        return activeExecution;
    }

    @Override
    public void setActiveExecution(WasmExecutor.ExecutionHandle handle) {
        this.activeExecution = handle;
    }

    @Override
    public void markChanged() {
        setChanged();
    }
}

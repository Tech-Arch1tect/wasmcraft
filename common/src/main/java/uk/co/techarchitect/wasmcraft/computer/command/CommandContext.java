package uk.co.techarchitect.wasmcraft.computer.command;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import uk.co.techarchitect.wasmcraft.computer.FileSystem;
import uk.co.techarchitect.wasmcraft.computer.Terminal;
import uk.co.techarchitect.wasmcraft.wasm.WasmExecutor;

public interface CommandContext {
    void addOutput(String line);

    Terminal getTerminal();

    FileSystem getFileSystem();

    BlockEntity getBlockEntity();

    Level getLevel();

    WasmExecutor.ExecutionHandle getActiveExecution();

    void setActiveExecution(WasmExecutor.ExecutionHandle handle);

    void markChanged();

    default void prepareForExecution() {
    }
}

package uk.co.techarchitect.wasmcraft.computer.command.builtin;

import uk.co.techarchitect.wasmcraft.computer.command.Command;
import uk.co.techarchitect.wasmcraft.computer.command.CommandContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmExecutor;

public class StopCommand implements Command {
    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getUsage() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Cancel running program";
    }

    @Override
    public void execute(CommandContext context, String[] args) {
        WasmExecutor.ExecutionHandle handle = context.getActiveExecution();
        if (handle == null || handle.isDone()) {
            context.addOutput("No program is currently running");
        } else {
            if (handle.cancel()) {
                context.addOutput("Program cancelled");
                context.setActiveExecution(null);
            } else {
                context.addOutput("Failed to cancel program");
            }
        }
    }
}

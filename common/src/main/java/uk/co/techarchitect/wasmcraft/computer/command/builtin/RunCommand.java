package uk.co.techarchitect.wasmcraft.computer.command.builtin;

import uk.co.techarchitect.wasmcraft.computer.command.Command;
import uk.co.techarchitect.wasmcraft.computer.command.CommandContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmExecutor;

import java.util.List;

public class RunCommand implements Command {
    private final WasmContext wasmContext;

    public RunCommand(WasmContext wasmContext) {
        this.wasmContext = wasmContext;
    }

    @Override
    public String getName() {
        return "run";
    }

    @Override
    public String getUsage() {
        return "run <file>";
    }

    @Override
    public String getDescription() {
        return "Execute WASM file";
    }

    @Override
    public void execute(CommandContext context, String[] args) {
        if (args.length < 1) {
            context.addOutput("Usage: run <filename>");
            context.addOutput("Example: run hello.wasm");
            return;
        }

        if (context.getActiveExecution() != null && !context.getActiveExecution().isDone()) {
            context.addOutput("ERROR: A program is already running. Use 'stop' to cancel it.");
            return;
        }

        String filename = args[0];
        if (!filename.endsWith(".wasm")) {
            filename = filename + ".wasm";
        }

        byte[] wasmBytes = context.getFileSystem().get(filename);
        if (wasmBytes == null) {
            context.addOutput("File not found: " + filename);
            return;
        }

        context.addOutput("Executing " + filename + "...");

        context.prepareForExecution();

        WasmExecutor.ExecutionHandle handle = WasmExecutor.executeAsync(wasmBytes, wasmContext);
        context.setActiveExecution(handle);
    }

    @Override
    public List<String> tabComplete(CommandContext context, String[] args) {
        if (args.length == 0 || args.length == 1) {
            return context.getFileSystem().listFiles();
        }
        return List.of();
    }
}

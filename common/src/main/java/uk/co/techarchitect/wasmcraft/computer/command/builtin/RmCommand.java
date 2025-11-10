package uk.co.techarchitect.wasmcraft.computer.command.builtin;

import uk.co.techarchitect.wasmcraft.computer.FileSystem;
import uk.co.techarchitect.wasmcraft.computer.command.Command;
import uk.co.techarchitect.wasmcraft.computer.command.CommandContext;

import java.util.List;

public class RmCommand implements Command {
    @Override
    public String getName() {
        return "rm";
    }

    @Override
    public String getUsage() {
        return "rm <file>";
    }

    @Override
    public String getDescription() {
        return "Delete file from storage";
    }

    @Override
    public void execute(CommandContext context, String[] args) {
        if (args.length < 1) {
            context.addOutput("Usage: rm <filename>");
            return;
        }

        String filename = args[0];
        if (!filename.endsWith(".wasm")) {
            filename = filename + ".wasm";
        }

        FileSystem fs = context.getFileSystem();
        if (fs.remove(filename)) {
            context.addOutput("Deleted " + filename);
            context.markChanged();
        } else {
            context.addOutput("File not found: " + filename);
        }
    }

    @Override
    public List<String> tabComplete(CommandContext context, String[] args) {
        if (args.length == 0 || args.length == 1) {
            return context.getFileSystem().listFiles();
        }
        return List.of();
    }
}

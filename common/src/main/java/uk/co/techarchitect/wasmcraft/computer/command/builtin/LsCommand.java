package uk.co.techarchitect.wasmcraft.computer.command.builtin;

import uk.co.techarchitect.wasmcraft.computer.FileSystem;
import uk.co.techarchitect.wasmcraft.computer.command.Command;
import uk.co.techarchitect.wasmcraft.computer.command.CommandContext;

import java.util.Map;

public class LsCommand implements Command {
    @Override
    public String getName() {
        return "ls";
    }

    @Override
    public String getUsage() {
        return "ls";
    }

    @Override
    public String getDescription() {
        return "List files in storage";
    }

    @Override
    public void execute(CommandContext context, String[] args) {
        FileSystem fs = context.getFileSystem();
        if (fs.isEmpty()) {
            context.addOutput("No files stored");
        } else {
            context.addOutput("Files:");
            for (Map.Entry<String, byte[]> entry : fs.getFiles().entrySet()) {
                String name = entry.getKey();
                int size = entry.getValue().length;
                context.addOutput(String.format("  %-20s %6d bytes", name, size));
            }
        }
    }
}

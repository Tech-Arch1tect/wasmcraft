package uk.co.techarchitect.wasmcraft.computer.command;

import java.util.List;

public interface Command {
    String getName();

    String getUsage();

    String getDescription();

    void execute(CommandContext context, String[] args);

    default List<String> tabComplete(CommandContext context, String[] args) {
        return List.of();
    }
}

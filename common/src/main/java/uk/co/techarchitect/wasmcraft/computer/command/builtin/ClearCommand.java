package uk.co.techarchitect.wasmcraft.computer.command.builtin;

import uk.co.techarchitect.wasmcraft.computer.command.Command;
import uk.co.techarchitect.wasmcraft.computer.command.CommandContext;

public class ClearCommand implements Command {
    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getUsage() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "Clear the terminal";
    }

    @Override
    public void execute(CommandContext context, String[] args) {
        context.getTerminal().clear();
    }
}

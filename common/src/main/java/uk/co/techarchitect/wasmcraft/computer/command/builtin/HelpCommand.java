package uk.co.techarchitect.wasmcraft.computer.command.builtin;

import uk.co.techarchitect.wasmcraft.computer.command.Command;
import uk.co.techarchitect.wasmcraft.computer.command.CommandContext;
import uk.co.techarchitect.wasmcraft.computer.command.CommandRegistry;

import java.util.List;

public class HelpCommand implements Command {
    private final CommandRegistry registry;

    public HelpCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getUsage() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show this help message";
    }

    @Override
    public void execute(CommandContext context, String[] args) {
        context.addOutput("Available commands:");
        for (Command command : registry.getAll()) {
            context.addOutput(String.format("  %-15s - %s", command.getUsage(), command.getDescription()));
        }
    }
}

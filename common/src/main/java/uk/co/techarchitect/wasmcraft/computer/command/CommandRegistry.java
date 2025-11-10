package uk.co.techarchitect.wasmcraft.computer.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public void register(Command command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    public Command get(String name) {
        return commands.get(name.toLowerCase());
    }

    public Collection<Command> getAll() {
        return commands.values();
    }

    public boolean has(String name) {
        return commands.containsKey(name.toLowerCase());
    }
}

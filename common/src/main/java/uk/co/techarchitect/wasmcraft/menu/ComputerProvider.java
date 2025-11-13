package uk.co.techarchitect.wasmcraft.menu;

import java.util.List;

public interface ComputerProvider {
    List<String> getOutputHistory();
    List<String> getCommandHistory();
    List<String> getFileNames();
    void executeCommand(String command);
}

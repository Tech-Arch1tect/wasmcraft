package uk.co.techarchitect.wasmcraft.client.screen;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import uk.co.techarchitect.wasmcraft.menu.ComputerMenu;
import uk.co.techarchitect.wasmcraft.network.ComputerCommandPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ComputerScreen extends AbstractContainerScreen<ComputerMenu> {
    private static final int BACKGROUND_COLOR = 0xFF1E1E1E;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int LINE_HEIGHT = 10;
    private static final int PADDING = 5;
    private static final int INPUT_HEIGHT = 20;

    private int scrollOffset = 0;
    private EditBox inputField;
    private int historyIndex = -1;
    private List<String> tabCompletions = new ArrayList<>();
    private int tabCompletionIndex = -1;
    private String tabCompletionPrefix = "";

    public ComputerScreen(ComputerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 350;
        this.imageHeight = 200;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();

        int inputY = this.topPos + this.imageHeight - INPUT_HEIGHT - PADDING;
        inputField = new EditBox(this.font, this.leftPos + PADDING, inputY,
                                 this.imageWidth - PADDING * 2, INPUT_HEIGHT, Component.literal(""));
        inputField.setMaxLength(256);
        inputField.setBordered(false);
        inputField.setTextColor(TEXT_COLOR);
        addRenderableWidget(inputField);
        setInitialFocus(inputField);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, BACKGROUND_COLOR);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        List<String> history = this.menu.getOutputHistory();

        int outputAreaHeight = this.imageHeight - INPUT_HEIGHT - PADDING * 3;
        int maxVisibleLines = outputAreaHeight / LINE_HEIGHT;
        int startLine = Math.max(0, history.size() - maxVisibleLines - scrollOffset);
        int endLine = Math.min(history.size(), startLine + maxVisibleLines);

        int y = this.topPos + PADDING;
        for (int i = startLine; i < endLine; i++) {
            String line = history.get(i);
            graphics.drawString(this.font, line, this.leftPos + PADDING, y, TEXT_COLOR, false);
            y += LINE_HEIGHT;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<String> history = this.menu.getOutputHistory();
        int outputAreaHeight = this.imageHeight - INPUT_HEIGHT - PADDING * 3;
        int maxVisibleLines = outputAreaHeight / LINE_HEIGHT;
        int maxScroll = Math.max(0, history.size() - maxVisibleLines);

        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) scrollY));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (inputField.isFocused()) {
            if (keyCode == 257 || keyCode == 335) {
                String command = inputField.getValue().trim();
                if (!command.isEmpty()) {
                    sendCommand(command);
                    inputField.setValue("");
                    historyIndex = -1;
                }
                return true;
            }

            if (keyCode == 265) {
                navigateHistory(true);
                return true;
            }

            if (keyCode == 264) {
                navigateHistory(false);
                return true;
            }

            if (keyCode == 258) {
                handleTabCompletion();
                return true;
            }

            resetTabCompletion();

            if (inputField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        if (keyCode == 256) {
            this.onClose();
            return true;
        }

        return false;
    }

    private void navigateHistory(boolean up) {
        List<String> history = this.menu.getCommandHistory();
        if (history.isEmpty()) {
            return;
        }

        if (up) {
            if (historyIndex < history.size() - 1) {
                historyIndex++;
            }
        } else {
            if (historyIndex > -1) {
                historyIndex--;
            }
        }

        if (historyIndex == -1) {
            inputField.setValue("");
        } else {
            int actualIndex = history.size() - 1 - historyIndex;
            inputField.setValue(history.get(actualIndex));
            inputField.moveCursorToEnd(false);
        }
    }

    private void handleTabCompletion() {
        String currentInput = inputField.getValue();

        if (tabCompletions.isEmpty() || !currentInput.equals(tabCompletionPrefix)) {
            tabCompletions = getCompletions(currentInput);
            tabCompletionIndex = -1;
            tabCompletionPrefix = currentInput;
        }

        if (tabCompletions.isEmpty()) {
            return;
        }

        tabCompletionIndex = (tabCompletionIndex + 1) % tabCompletions.size();
        inputField.setValue(tabCompletions.get(tabCompletionIndex));
        inputField.moveCursorToEnd(false);
    }

    private void resetTabCompletion() {
        tabCompletions.clear();
        tabCompletionIndex = -1;
        tabCompletionPrefix = "";
    }

    private List<String> getCompletions(String input) {
        if (input.isEmpty()) {
            return new ArrayList<>(List.of("help", "clear", "ls", "rm ", "download ", "run ", "stop"));
        }

        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();

        if (parts.length == 1) {
            List<String> commands = List.of("help", "clear", "ls", "rm", "download", "run", "stop");
            return commands.stream()
                    .filter(cmd -> cmd.startsWith(command))
                    .collect(Collectors.toList());
        }

        if (command.equals("rm") || command.equals("run")) {
            String prefix = parts.length > 1 ? parts[1] : "";
            return getFileCompletions(command, prefix);
        }

        return new ArrayList<>();
    }

    private List<String> getFileCompletions(String command, String prefix) {
        List<String> files = menu.getFileNames();
        String lowerPrefix = prefix.toLowerCase();

        return files.stream()
                .filter(file -> file.toLowerCase().startsWith(lowerPrefix))
                .map(file -> command + " " + file)
                .collect(Collectors.toList());
    }

    private void sendCommand(String command) {
        if (menu.isBlockEntity()) {
            NetworkManager.sendToServer(new ComputerCommandPacket(menu.getBlockPos(), command, -1));
        } else if (menu.isEntity()) {
            NetworkManager.sendToServer(new ComputerCommandPacket(null, command, menu.getEntityId()));
        }
    }
}

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
    private static final int TAB_SIZE = 20;

    private static final int TERMINAL_WIDTH = 350;
    private static final int TERMINAL_HEIGHT = 200;
    private static final int INVENTORY_WIDTH = 176;

    private int scrollOffset = 0;
    private EditBox inputField;
    private int historyIndex = -1;
    private List<String> tabCompletions = new ArrayList<>();
    private int tabCompletionIndex = -1;
    private String tabCompletionPrefix = "";

    public ComputerScreen(ComputerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        if (menu.hasInventory()) {
            if (menu.getCurrentTab() == ComputerMenu.Tab.TERMINAL) {
                this.imageWidth = TERMINAL_WIDTH + TAB_SIZE;
                this.imageHeight = TERMINAL_HEIGHT;
            } else {
                this.imageWidth = INVENTORY_WIDTH + TAB_SIZE;
                int rows = menu.getInventorySize() / 9;
                this.imageHeight = 18 + (rows * 18) + 14 + 76;
            }
        } else {
            this.imageWidth = TERMINAL_WIDTH;
            this.imageHeight = TERMINAL_HEIGHT;
        }

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();

        if (!menu.hasInventory() || menu.getCurrentTab() == ComputerMenu.Tab.TERMINAL) {
            int tabSpace = menu.hasInventory() ? TAB_SIZE : 0;
            int inputY = this.topPos + TERMINAL_HEIGHT - INPUT_HEIGHT - PADDING;
            int inputWidth = this.imageWidth - PADDING * 2 - tabSpace;

            inputField = new EditBox(this.font, this.leftPos + PADDING, inputY,
                                     inputWidth, INPUT_HEIGHT, Component.literal(""));
            inputField.setMaxLength(256);
            inputField.setBordered(false);
            inputField.setTextColor(TEXT_COLOR);
            addRenderableWidget(inputField);
            setInitialFocus(inputField);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (menu.hasInventory()) {
            if (menu.getCurrentTab() == ComputerMenu.Tab.TERMINAL) {
                graphics.fill(this.leftPos, this.topPos, this.leftPos + TERMINAL_WIDTH, this.topPos + this.imageHeight, BACKGROUND_COLOR);
            } else {
                graphics.fill(this.leftPos, this.topPos, this.leftPos + INVENTORY_WIDTH, this.topPos + this.imageHeight, 0xFFC6C6C6);

                int rows = menu.getInventorySize() / 9;
                int droneInvY = 18;
                int playerInvY = droneInvY + (rows * 18) + 14;

                graphics.drawString(this.font, "Drone", this.leftPos + 8, this.topPos + 6, 0xFF404040, false);
                graphics.drawString(this.font, "Inventory", this.leftPos + 8, this.topPos + playerInvY - 12, 0xFF404040, false);

                renderSlotBackgrounds(graphics, rows, droneInvY, playerInvY);
            }
            renderTabs(graphics);
        } else {
            graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, BACKGROUND_COLOR);
        }
    }

    private void renderSlotBackgrounds(GuiGraphics graphics, int droneRows, int droneInvY, int playerInvY) {
        int slotX = this.leftPos + 7;
        int slotY = this.topPos + droneInvY - 1;

        for (int row = 0; row < droneRows; row++) {
            for (int col = 0; col < 9; col++) {
                int x = slotX + col * 18;
                int y = slotY + row * 18;
                renderSlotBackground(graphics, x, y);
            }
        }

        slotX = this.leftPos + 7;
        slotY = this.topPos + playerInvY - 1;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int x = slotX + col * 18;
                int y = slotY + row * 18;
                renderSlotBackground(graphics, x, y);
            }
        }

        slotY = this.topPos + playerInvY + 58 - 1;
        for (int col = 0; col < 9; col++) {
            int x = slotX + col * 18;
            renderSlotBackground(graphics, x, slotY);
        }
    }

    private void renderSlotBackground(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        graphics.fill(x, y, x + 18, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + 18, 0xFF373737);
        graphics.fill(x + 17, y + 1, x + 18, y + 18, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
    }

    private void renderTabs(GuiGraphics graphics) {
        int tabX = this.leftPos + (menu.getCurrentTab() == ComputerMenu.Tab.TERMINAL ? TERMINAL_WIDTH : INVENTORY_WIDTH);
        int tabY = this.topPos;

        boolean terminalActive = menu.getCurrentTab() == ComputerMenu.Tab.TERMINAL;
        int terminalColor = terminalActive ? 0xFF505050 : 0xFF303030;
        graphics.fill(tabX, tabY, tabX + TAB_SIZE, tabY + TAB_SIZE, terminalColor);
        graphics.fill(tabX, tabY, tabX + TAB_SIZE, tabY + 1, 0xFF707070);
        graphics.fill(tabX, tabY, tabX + 1, tabY + TAB_SIZE, 0xFF707070);
        graphics.drawString(this.font, "T", tabX + 7, tabY + 6, terminalActive ? 0xFFFFFFFF : 0xFF808080, false);

        tabY += TAB_SIZE;
        boolean inventoryActive = menu.getCurrentTab() == ComputerMenu.Tab.INVENTORY;
        int inventoryColor = inventoryActive ? 0xFF505050 : 0xFF303030;
        graphics.fill(tabX, tabY, tabX + TAB_SIZE, tabY + TAB_SIZE, inventoryColor);
        graphics.fill(tabX, tabY, tabX + TAB_SIZE, tabY + 1, 0xFF707070);
        graphics.fill(tabX, tabY, tabX + 1, tabY + TAB_SIZE, 0xFF707070);
        graphics.drawString(this.font, "I", tabX + 8, tabY + 6, inventoryActive ? 0xFFFFFFFF : 0xFF808080, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (menu.hasInventory() && menu.getCurrentTab() == ComputerMenu.Tab.INVENTORY) {
            super.render(graphics, mouseX, mouseY, partialTick);
        } else {
            this.renderBackground(graphics, mouseX, mouseY, partialTick);
            this.renderBg(graphics, partialTick, mouseX, mouseY);

            if (!menu.hasInventory() || menu.getCurrentTab() == ComputerMenu.Tab.TERMINAL) {
                List<String> history = this.menu.getOutputHistory();

                int outputAreaHeight = TERMINAL_HEIGHT - INPUT_HEIGHT - PADDING * 3;
                int maxVisibleLines = outputAreaHeight / LINE_HEIGHT;
                int startLine = Math.max(0, history.size() - maxVisibleLines - scrollOffset);
                int endLine = Math.min(history.size(), startLine + maxVisibleLines);

                int y = this.topPos + PADDING;
                for (int i = startLine; i < endLine; i++) {
                    String line = history.get(i);
                    graphics.drawString(this.font, line, this.leftPos + PADDING, y, TEXT_COLOR, false);
                    y += LINE_HEIGHT;
                }

                if (inputField != null) {
                    inputField.render(graphics, mouseX, mouseY, partialTick);
                }
            }
        }
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, net.minecraft.world.inventory.Slot slot) {
        if (menu.hasInventory() && menu.getCurrentTab() == ComputerMenu.Tab.INVENTORY) {
            super.renderSlot(graphics, slot);
        }
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        if (menu.hasInventory() && menu.getCurrentTab() == ComputerMenu.Tab.TERMINAL) {
            return false;
        }
        return super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.hasInventory() && menu.getCurrentTab() == ComputerMenu.Tab.TERMINAL) {
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        if (menu.hasInventory() && menu.getCurrentTab() == ComputerMenu.Tab.TERMINAL) {
            return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.hasInventory() && button == 0) {
            int contentWidth = menu.getCurrentTab() == ComputerMenu.Tab.TERMINAL ? TERMINAL_WIDTH : INVENTORY_WIDTH;
            int relativeX = (int) (mouseX - this.leftPos);
            int relativeY = (int) (mouseY - this.topPos);

            if (relativeX >= contentWidth && relativeX < contentWidth + TAB_SIZE) {
                if (relativeY >= 0 && relativeY < TAB_SIZE) {
                    menu.setCurrentTab(ComputerMenu.Tab.TERMINAL);
                    this.minecraft.setScreen(new ComputerScreen(menu, this.minecraft.player.getInventory(), this.title));
                    return true;
                } else if (relativeY >= TAB_SIZE && relativeY < TAB_SIZE * 2) {
                    menu.setCurrentTab(ComputerMenu.Tab.INVENTORY);
                    this.minecraft.setScreen(new ComputerScreen(menu, this.minecraft.player.getInventory(), this.title));
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!menu.hasInventory() || menu.getCurrentTab() == ComputerMenu.Tab.TERMINAL) {
            List<String> history = this.menu.getOutputHistory();
            int outputAreaHeight = TERMINAL_HEIGHT - INPUT_HEIGHT - PADDING * 3;
            int maxVisibleLines = outputAreaHeight / LINE_HEIGHT;
            int maxScroll = Math.max(0, history.size() - maxVisibleLines);

            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) scrollY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (inputField != null && inputField.isFocused()) {
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

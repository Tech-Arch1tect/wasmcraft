package uk.co.techarchitect.wasmcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import uk.co.techarchitect.wasmcraft.menu.ComputerMenu;

import java.util.List;

public class ComputerScreen extends AbstractContainerScreen<ComputerMenu> {
    private static final int BACKGROUND_COLOR = 0xFF1E1E1E;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int LINE_HEIGHT = 10;
    private static final int PADDING = 5;

    private int scrollOffset = 0;

    public ComputerScreen(ComputerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 350;
        this.imageHeight = 200;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, BACKGROUND_COLOR);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        List<String> history = this.menu.getOutputHistory();

        int maxVisibleLines = (this.imageHeight - PADDING * 2) / LINE_HEIGHT;
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
        int maxVisibleLines = (this.imageHeight - PADDING * 2) / LINE_HEIGHT;
        int maxScroll = Math.max(0, history.size() - maxVisibleLines);

        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) scrollY));
        return true;
    }
}

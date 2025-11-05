package uk.co.techarchitect.wasmcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import uk.co.techarchitect.wasmcraft.menu.ComputerMenu;

public class ComputerScreen extends AbstractContainerScreen<ComputerMenu> {
    private static final int BACKGROUND_COLOR = 0xFF1E1E1E;
    private static final int TEXT_COLOR = 0xFFE0E0E0;

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

        graphics.drawString(this.font, "Computer Terminal", this.leftPos + 10, this.topPos + 10, TEXT_COLOR, false);
    }
}

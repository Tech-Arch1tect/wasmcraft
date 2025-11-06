package uk.co.techarchitect.wasmcraft.client.screen;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import uk.co.techarchitect.wasmcraft.network.packet.PeripheralLabelPacket;

public class PeripheralLabelScreen extends Screen {
    private final BlockPos pos;
    private final String currentLabel;
    private EditBox labelInput;

    public PeripheralLabelScreen(BlockPos pos, String currentLabel) {
        super(Component.literal("Set Peripheral Label"));
        this.pos = pos;
        this.currentLabel = currentLabel;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        labelInput = new EditBox(this.font, centerX - 100, centerY - 10, 200, 20, Component.literal("Label"));
        labelInput.setMaxLength(32);
        labelInput.setValue(currentLabel != null ? currentLabel : "");
        labelInput.setFocused(true);
        addRenderableWidget(labelInput);

        addRenderableWidget(Button.builder(Component.literal("Set Label"), button -> {
            String label = labelInput.getValue().trim();
            if (!label.isEmpty()) {
                NetworkManager.sendToServer(new PeripheralLabelPacket(pos, label));
            }
            this.minecraft.setScreen(null);
        }).bounds(centerX - 100, centerY + 20, 95, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> {
            this.minecraft.setScreen(null);
        }).bounds(centerX + 5, centerY + 20, 95, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);

        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        graphics.drawCenteredString(this.font, "Set Peripheral Label", centerX, centerY - 40, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

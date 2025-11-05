package uk.co.techarchitect.wasmcraft.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import uk.co.techarchitect.wasmcraft.blockentity.ComputerBlockEntity;

public class ComputerMenu extends AbstractContainerMenu {
    private final ComputerBlockEntity blockEntity;

    public ComputerMenu(int containerId, Inventory playerInventory, ComputerBlockEntity blockEntity) {
        super(ModMenuTypes.COMPUTER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public ComputerBlockEntity getBlockEntity() {
        return blockEntity;
    }
}

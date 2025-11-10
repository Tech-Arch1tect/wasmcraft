package uk.co.techarchitect.wasmcraft.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import uk.co.techarchitect.wasmcraft.blockentity.ComputerBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class ComputerMenu extends AbstractContainerMenu {
    private final ComputerBlockEntity blockEntity;
    private List<String> clientOutputHistory = new ArrayList<>();
    private List<String> clientCommandHistory = new ArrayList<>();

    public ComputerMenu(int containerId, Inventory playerInventory, ComputerBlockEntity blockEntity) {
        super(ModMenuTypes.COMPUTER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        if (blockEntity != null) {
            this.clientOutputHistory = new ArrayList<>(blockEntity.getOutputHistory());
            this.clientCommandHistory = new ArrayList<>(blockEntity.getCommandHistory());
        }
    }

    public ComputerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, (ComputerBlockEntity) playerInventory.player.level().getBlockEntity(pos));
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

    public List<String> getOutputHistory() {
        return clientOutputHistory;
    }

    public void setClientOutputHistory(List<String> history) {
        this.clientOutputHistory = new ArrayList<>(history);
    }

    public List<String> getCommandHistory() {
        return clientCommandHistory;
    }

    public void setClientCommandHistory(List<String> history) {
        this.clientCommandHistory = new ArrayList<>(history);
    }
}

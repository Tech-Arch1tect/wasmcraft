package uk.co.techarchitect.wasmcraft.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import uk.co.techarchitect.wasmcraft.computer.ComputerBlockEntityBase;

import java.util.ArrayList;
import java.util.List;

public class ComputerMenu extends AbstractContainerMenu {
    private final ComputerBlockEntityBase blockEntity;
    private List<String> clientOutputHistory = new ArrayList<>();
    private List<String> clientCommandHistory = new ArrayList<>();
    private List<String> clientFileNames = new ArrayList<>();

    public ComputerMenu(int containerId, Inventory playerInventory, ComputerBlockEntityBase blockEntity) {
        super(ModMenuTypes.COMPUTER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        if (blockEntity != null) {
            this.clientOutputHistory = new ArrayList<>(blockEntity.getOutputHistory());
            this.clientCommandHistory = new ArrayList<>(blockEntity.getCommandHistory());
            this.clientFileNames = new ArrayList<>(blockEntity.getFileNames());
        }
    }

    public ComputerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, (ComputerBlockEntityBase) playerInventory.player.level().getBlockEntity(pos));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public ComputerBlockEntityBase getBlockEntity() {
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

    public List<String> getFileNames() {
        return clientFileNames;
    }

    public void setClientFileNames(List<String> fileNames) {
        this.clientFileNames = new ArrayList<>(fileNames);
    }
}

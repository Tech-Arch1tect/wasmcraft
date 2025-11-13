package uk.co.techarchitect.wasmcraft.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ComputerMenu extends AbstractContainerMenu {
    private final ComputerProvider provider;
    private final BlockPos blockPos;
    private final int entityId;
    private List<String> clientOutputHistory = new ArrayList<>();
    private List<String> clientCommandHistory = new ArrayList<>();
    private List<String> clientFileNames = new ArrayList<>();

    public ComputerMenu(int containerId, Inventory playerInventory, ComputerProvider provider, BlockPos blockPos, int entityId) {
        super(ModMenuTypes.COMPUTER_MENU.get(), containerId);
        this.provider = provider;
        this.blockPos = blockPos;
        this.entityId = entityId;
        if (provider != null) {
            this.clientOutputHistory = new ArrayList<>(provider.getOutputHistory());
            this.clientCommandHistory = new ArrayList<>(provider.getCommandHistory());
            this.clientFileNames = new ArrayList<>(provider.getFileNames());
        }
    }

    public ComputerMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory,
             (ComputerProvider) playerInventory.player.level().getBlockEntity(pos),
             pos, -1);
    }

    public ComputerMenu(int containerId, Inventory playerInventory, int entityId) {
        this(containerId, playerInventory,
             (ComputerProvider) playerInventory.player.level().getEntity(entityId),
             null, entityId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public ComputerProvider getProvider() {
        return provider;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public int getEntityId() {
        return entityId;
    }

    public boolean isBlockEntity() {
        return blockPos != null;
    }

    public boolean isEntity() {
        return entityId != -1;
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

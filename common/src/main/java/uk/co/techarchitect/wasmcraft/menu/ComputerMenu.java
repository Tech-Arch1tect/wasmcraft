package uk.co.techarchitect.wasmcraft.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ComputerMenu extends AbstractContainerMenu {
    public enum Tab {
        TERMINAL,
        INVENTORY
    }

    private final ComputerProvider provider;
    private final BlockPos blockPos;
    private final int entityId;
    private final Container inventory;
    private final int inventorySize;
    private Tab currentTab = Tab.TERMINAL;
    private List<String> clientOutputHistory = new ArrayList<>();
    private List<String> clientCommandHistory = new ArrayList<>();
    private List<String> clientFileNames = new ArrayList<>();

    public ComputerMenu(int containerId, Inventory playerInventory, ComputerProvider provider, BlockPos blockPos, int entityId) {
        super(ModMenuTypes.COMPUTER_MENU.get(), containerId);
        this.provider = provider;
        this.blockPos = blockPos;
        this.entityId = entityId;

        if (provider instanceof InventoryProvider inventoryProvider) {
            this.inventory = inventoryProvider.getInventory();
            this.inventorySize = inventoryProvider.getInventorySize();
        } else {
            this.inventory = null;
            this.inventorySize = 0;
        }

        if (provider != null) {
            this.clientOutputHistory = new ArrayList<>(provider.getOutputHistory());
            this.clientCommandHistory = new ArrayList<>(provider.getCommandHistory());
            this.clientFileNames = new ArrayList<>(provider.getFileNames());
        }

        if (inventory != null) {
            addInventorySlots(playerInventory);
        }
    }

    private void addInventorySlots(Inventory playerInventory) {
        int computerInvX = 8;
        int computerInvY = 18;
        int rows = inventorySize / 9;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9, computerInvX + col * 18, computerInvY + row * 18));
            }
        }

        int playerInvX = 8;
        int playerInvY = computerInvY + (rows * 18) + 14;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, 9 + col + row * 9, playerInvX + col * 18, playerInvY + row * 18));
            }
        }

        int hotbarY = playerInvY + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, playerInvX + col * 18, hotbarY));
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
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (inventory == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stackCopy = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            stackCopy = stack.copy();

            if (slotIndex < inventorySize) {
                if (!this.moveItemStackTo(stack, inventorySize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, 0, inventorySize, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == stackCopy.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return stackCopy;
    }

    @Override
    public boolean stillValid(Player player) {
        if (inventory != null) {
            return inventory.stillValid(player);
        }
        return true;
    }

    public boolean hasInventory() {
        return inventory != null;
    }

    public int getInventorySize() {
        return inventorySize;
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

    public Tab getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(Tab tab) {
        this.currentTab = tab;
    }
}

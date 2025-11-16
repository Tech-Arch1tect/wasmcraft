package uk.co.techarchitect.wasmcraft.menu;

import net.minecraft.world.Container;

public interface InventoryProvider {
    Container getInventory();
    int getInventorySize();
}

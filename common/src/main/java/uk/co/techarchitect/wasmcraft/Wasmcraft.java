package uk.co.techarchitect.wasmcraft;

import uk.co.techarchitect.wasmcraft.block.ModBlocks;
import uk.co.techarchitect.wasmcraft.item.ModCreativeTabs;
import uk.co.techarchitect.wasmcraft.item.ModItems;

public final class Wasmcraft {
    public static final String MOD_ID = "wasmcraft";

    public static void init() {
        ModBlocks.register();
        ModItems.register();
        ModCreativeTabs.register();
    }
}

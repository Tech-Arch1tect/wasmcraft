package uk.co.techarchitect.wasmcraft;

import uk.co.techarchitect.wasmcraft.block.ModBlocks;
import uk.co.techarchitect.wasmcraft.blockentity.ModBlockEntities;
import uk.co.techarchitect.wasmcraft.item.ModCreativeTabs;
import uk.co.techarchitect.wasmcraft.item.ModItems;
import uk.co.techarchitect.wasmcraft.menu.ModMenuTypes;
import uk.co.techarchitect.wasmcraft.network.ModNetworking;

public final class Wasmcraft {
    public static final String MOD_ID = "wasmcraft";

    public static void init() {
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModMenuTypes.register();
        ModCreativeTabs.register();
        ModNetworking.register();
    }
}

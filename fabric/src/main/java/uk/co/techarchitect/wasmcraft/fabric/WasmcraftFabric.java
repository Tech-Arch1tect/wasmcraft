package uk.co.techarchitect.wasmcraft.fabric;

import uk.co.techarchitect.wasmcraft.Wasmcraft;
import uk.co.techarchitect.wasmcraft.chunkloading.ChunkLoadingManager;
import net.fabricmc.api.ModInitializer;

public final class WasmcraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        ChunkLoadingManager.getInstance().setProvider(new FabricChunkLoadingProvider());

        // Run our common setup.
        Wasmcraft.init();
    }
}

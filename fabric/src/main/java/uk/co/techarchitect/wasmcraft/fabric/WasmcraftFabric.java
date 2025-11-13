package uk.co.techarchitect.wasmcraft.fabric;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import uk.co.techarchitect.wasmcraft.Wasmcraft;
import uk.co.techarchitect.wasmcraft.chunkloading.ChunkLoadingManager;
import net.fabricmc.api.ModInitializer;
import uk.co.techarchitect.wasmcraft.entity.DroneEntity;
import uk.co.techarchitect.wasmcraft.entity.ModEntities;

public final class WasmcraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        ChunkLoadingManager.getInstance().setProvider(new FabricChunkLoadingProvider());

        // Run our common setup.
        Wasmcraft.init();

        // Register entity attributes
        FabricDefaultAttributeRegistry.register(ModEntities.DRONE.get(), DroneEntity.createAttributes());
    }
}

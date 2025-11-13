package uk.co.techarchitect.wasmcraft.neoforge;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import uk.co.techarchitect.wasmcraft.Wasmcraft;
import uk.co.techarchitect.wasmcraft.chunkloading.ChunkLoadingManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import uk.co.techarchitect.wasmcraft.entity.DroneEntity;
import uk.co.techarchitect.wasmcraft.entity.ModEntities;

@Mod(Wasmcraft.MOD_ID)
public final class WasmcraftNeoForge {
    public static final TicketController CHUNK_TICKET_CONTROLLER = new TicketController(
        ResourceLocation.fromNamespaceAndPath(Wasmcraft.MOD_ID, "computer_chunk_loader")
    );

    public WasmcraftNeoForge(IEventBus modBus) {
        modBus.addListener(this::registerTicketControllers);
        modBus.addListener(this::registerEntityAttributes);

        ChunkLoadingManager.getInstance().setProvider(new NeoForgeChunkLoadingProvider());

        // Run our common setup.
        Wasmcraft.init();
    }

    private void registerTicketControllers(RegisterTicketControllersEvent event) {
        event.register(CHUNK_TICKET_CONTROLLER);
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DRONE.get(), DroneEntity.createAttributes().build());
    }
}


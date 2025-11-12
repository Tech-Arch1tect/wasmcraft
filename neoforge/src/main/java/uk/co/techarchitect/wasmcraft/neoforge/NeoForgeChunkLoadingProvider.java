package uk.co.techarchitect.wasmcraft.neoforge;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import uk.co.techarchitect.wasmcraft.chunkloading.ChunkLoadingProvider;

import java.util.UUID;

public class NeoForgeChunkLoadingProvider implements ChunkLoadingProvider {
    @Override
    public void forceLoadChunk(ServerLevel level, ChunkPos chunkPos, UUID loaderId) {
        WasmcraftNeoForge.CHUNK_TICKET_CONTROLLER.forceChunk(level, loaderId, chunkPos.x, chunkPos.z, true, false);
    }

    @Override
    public void releaseChunk(ServerLevel level, ChunkPos chunkPos, UUID loaderId) {
        WasmcraftNeoForge.CHUNK_TICKET_CONTROLLER.forceChunk(level, loaderId, chunkPos.x, chunkPos.z, false, false);
    }
}

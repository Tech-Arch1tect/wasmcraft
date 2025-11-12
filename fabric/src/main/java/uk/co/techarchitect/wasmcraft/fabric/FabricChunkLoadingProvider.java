package uk.co.techarchitect.wasmcraft.fabric;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import uk.co.techarchitect.wasmcraft.chunkloading.ChunkLoadingProvider;

import java.util.UUID;

public class FabricChunkLoadingProvider implements ChunkLoadingProvider {
    @Override
    public void forceLoadChunk(ServerLevel level, ChunkPos chunkPos, UUID loaderId) {
        level.setChunkForced(chunkPos.x, chunkPos.z, true);
    }

    @Override
    public void releaseChunk(ServerLevel level, ChunkPos chunkPos, UUID loaderId) {
        level.setChunkForced(chunkPos.x, chunkPos.z, false);
    }
}

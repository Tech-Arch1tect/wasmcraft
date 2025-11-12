package uk.co.techarchitect.wasmcraft.chunkloading;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public interface ChunkLoadingProvider {
    void forceLoadChunk(ServerLevel level, ChunkPos chunkPos, UUID loaderId);
    void releaseChunk(ServerLevel level, ChunkPos chunkPos, UUID loaderId);
}

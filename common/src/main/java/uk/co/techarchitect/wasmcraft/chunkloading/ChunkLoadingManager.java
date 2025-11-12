package uk.co.techarchitect.wasmcraft.chunkloading;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkLoadingManager {
    private static final ChunkLoadingManager INSTANCE = new ChunkLoadingManager();

    private final Map<UUID, ChunkLoader> activeLoaders = new ConcurrentHashMap<>();
    private ChunkLoadingProvider provider;

    private ChunkLoadingManager() {}

    public static ChunkLoadingManager getInstance() {
        return INSTANCE;
    }

    public void setProvider(ChunkLoadingProvider provider) {
        this.provider = provider;
    }

    public void registerChunkLoader(UUID loaderId, ServerLevel level, BlockPos pos) {
        if (provider == null) {
            return;
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        ChunkLoader loader = activeLoaders.computeIfAbsent(loaderId, id -> new ChunkLoader(id, level, chunkPos));

        loader.setPosition(chunkPos);
        loader.ensureChunksLoaded(provider);
    }

    public void unregisterChunkLoader(UUID loaderId) {
        if (provider == null) {
            return;
        }

        ChunkLoader loader = activeLoaders.remove(loaderId);
        if (loader != null) {
            loader.releaseAllChunks(provider);
        }
    }

    public void updateLoaderPosition(UUID loaderId, ServerLevel level, BlockPos newPos) {
        if (provider == null) {
            return;
        }

        ChunkLoader loader = activeLoaders.get(loaderId);
        if (loader == null) {
            registerChunkLoader(loaderId, level, newPos);
            return;
        }

        ChunkPos newChunkPos = new ChunkPos(newPos);
        ChunkPos oldChunkPos = loader.getChunkPos();

        if (!newChunkPos.equals(oldChunkPos)) {
            loader.releaseAllChunks(provider);
            loader.setPosition(newChunkPos);
            loader.ensureChunksLoaded(provider);
        }
    }

    public Set<ChunkPos> getLoadedChunks(UUID loaderId) {
        ChunkLoader loader = activeLoaders.get(loaderId);
        if (loader == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(loader.getLoadedChunks());
    }

    public void cleanup() {
        activeLoaders.entrySet().removeIf(entry -> {
            ChunkLoader loader = entry.getValue();
            return loader.getLevel() == null;
        });
    }

    public static class ChunkLoader {
        private final UUID id;
        private final WeakReference<ServerLevel> levelRef;
        private ChunkPos currentChunkPos;
        private final Set<ChunkPos> loadedChunks = new HashSet<>();

        public ChunkLoader(UUID id, ServerLevel level, ChunkPos initialPos) {
            this.id = id;
            this.levelRef = new WeakReference<>(level);
            this.currentChunkPos = initialPos;
        }

        public UUID getId() {
            return id;
        }

        public ServerLevel getLevel() {
            return levelRef.get();
        }

        public ChunkPos getChunkPos() {
            return currentChunkPos;
        }

        public void setPosition(ChunkPos pos) {
            this.currentChunkPos = pos;
        }

        public Set<ChunkPos> getLoadedChunks() {
            return Collections.unmodifiableSet(loadedChunks);
        }

        public void ensureChunksLoaded(ChunkLoadingProvider provider) {
            ServerLevel level = getLevel();
            if (level == null) {
                return;
            }

            loadedChunks.add(currentChunkPos);
            provider.forceLoadChunk(level, currentChunkPos, id);
        }

        public void releaseAllChunks(ChunkLoadingProvider provider) {
            ServerLevel level = getLevel();
            if (level == null) {
                return;
            }

            for (ChunkPos chunkPos : loadedChunks) {
                provider.releaseChunk(level, chunkPos, id);
            }
            loadedChunks.clear();
        }
    }
}

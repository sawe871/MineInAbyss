package com.derongan.minecraft.mineinabyss.World;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.MineInAbyss;
import com.derongan.minecraft.mineinabyss.util.TickUtils;
import com.google.common.cache.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.entity.Entity;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EntityChunkManagerImpl implements EntityChunkManager {
    private static final long DELETE_DELAY = TickUtils.milisecondsToTicks(TimeUnit.SECONDS.toMillis(5));
    // This map contains entities for loaded chunks
    private Map<ChunkKey, Map<Point, ChunkEntity>> loadedChunkMap;

    // This map contains chunkentities for chunks that are not yet loaded or have been unloaded.
    private LoadingCache<ChunkKey, Map<Point, ChunkEntity>> unloadedChunkCache;

    // This map contains a map of actual entity UUIDs to chunkentities.
    private Map<UUID, ChunkEntity> chunkEntityMap;
    private AbyssWorldManager manager;
    private AbyssContext context;
    private WorldDataConfigManager configManager;

    public EntityChunkManagerImpl(AbyssContext context) {
        loadedChunkMap = new HashMap<>(100);
        chunkEntityMap = new HashMap<>(100);

        configManager = new WorldDataConfigManager(context);

        this.context = context;
        this.manager = context.getWorldManager();

        //TODO tune cache.
        // We save to disk once we lose an item from cache
        unloadedChunkCache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .maximumSize(500)
                .removalListener((RemovalListener<ChunkKey, Map<Point, ChunkEntity>>) notif -> flushToDisk(notif.getKey().toChunk(), notif.getValue()))
                .build(new CacheLoader<ChunkKey, Map<Point, ChunkEntity>>() {
                    @Override
                    public Map<Point, ChunkEntity> load(ChunkKey chunkKey) throws Exception {
                        return configManager.loadChunkData(chunkKey.toChunk());
                    }
                });


        //Initialize removal task
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
                MineInAbyss.getInstance(),
                new ExpiredEntitiesRemover(),
                0L,
                DELETE_DELAY
        );
    }

    @Override
    public void loadChunk(Chunk chunk) {
        ChunkKey chunkKey = new ChunkKey(chunk);

        Map<Point, ChunkEntity> chunkEntities = unloadedChunkCache.getIfPresent(chunkKey);

        // If its not in the cache, load it. Otherwise evict and move to loaded map.
        // Note that getIfPresent does not force loading.
        if (chunkEntities == null) {
            chunkEntities = configManager.loadChunkData(chunk);
        } else {
            unloadedChunkCache.invalidate(chunkKey);
        }

        loadedChunkMap.put(chunkKey, chunkEntities);

        // Remove all expired entities
        chunkEntities.values().removeIf(ce -> ce.getExpiration() < ce.getCurrentTime());

        chunkEntities.forEach((point, ce) -> {
            Entity e = ce.createEntity(chunk.getWorld());
            chunkEntityMap.put(e.getUniqueId(), ce);
        });

    }

    @Override
    public void unloadChunk(Chunk chunk) {
        ChunkKey chunkKey = new ChunkKey(chunk);

        Map<Point, ChunkEntity> entities = loadedChunkMap.getOrDefault(chunkKey, new HashMap<>());
        loadedChunkMap.remove(chunkKey);

        // Move the unloaded chunk to the cache.
        unloadedChunkCache.put(chunkKey, entities);

        entities.forEach((a, ce) -> {
            chunkEntityMap.remove(ce.getEntity().getUniqueId());
            ce.destroyEntity();
        });
    }

    @Override
    public void addEntity(Chunk chunk, ChunkEntity chunkEntity) {
        ChunkKey key = new ChunkKey(chunk);

        loadedChunkMap.computeIfAbsent(key, (a) -> new HashMap<>());

        loadedChunkMap.get(key).put(new Point(chunkEntity.getX(), chunkEntity.getY(), chunkEntity.getZ()), chunkEntity);

        if (chunk.isLoaded()) {
            chunkEntity.createEntity(chunk.getWorld());
            chunkEntityMap.put(chunkEntity.getEntity().getUniqueId(), chunkEntity);
        }
    }

    @Override
    public void removeEntity(Chunk chunk, Entity entity) {
        ChunkEntity e = chunkEntityMap.get(entity.getUniqueId());
        loadedChunkMap.get(new ChunkKey(chunk)).remove(new Point(e.getX(),e.getY(), e.getZ()));
        chunkEntityMap.remove(entity.getUniqueId());
        e.destroyEntity();
    }

    @Override
    public boolean isEntityRegistered(UUID uuid) {
        return chunkEntityMap.containsKey(uuid);
    }

    @Override
    public boolean isEntityAt(Chunk chunk, int x, int y, int z) {
        ChunkKey chunkKey = new ChunkKey(chunk);

        Map<Point, ChunkEntity> chunkEntityMap;

        if (loadedChunkMap.containsKey(chunkKey)) {
            chunkEntityMap = loadedChunkMap.get(chunkKey);
        } else {
            chunkEntityMap = unloadedChunkCache.getUnchecked(chunkKey);
        }

        return chunkEntityMap.containsKey(new Point(x, y, z));
    }

    @Override
    public void disable() {
        // Flush all from loaded chunks.
        Bukkit.getServer().getWorlds().forEach(a -> {
            Arrays.stream(a.getLoadedChunks()).forEach(this::unloadChunk);
        });

        unloadedChunkCache.invalidateAll();
    }

    private void flushToDisk(Chunk chunk, Map<Point, ChunkEntity> entities) {
        try {
            configManager.saveChunkData(chunk, entities);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ExpiredEntitiesRemover implements Runnable {
        @Override
        public void run() {
            loadedChunkMap.forEach((ck, ents) -> {
                Collection<ChunkEntity> badEnts = ents.values().stream().filter(ce -> ce.getExpiration() < ce.getCurrentTime()).collect(Collectors.toList());
                Chunk chunk = ck.toChunk();
                badEnts.forEach(a -> removeEntity(chunk, a.getEntity()));
            });
        }
    }

    private class ChunkKey {
        int x;
        int z;
        String worldName;

        ChunkKey(Chunk chunk) {
            this.x = chunk.getX();
            this.z = chunk.getZ();

            this.worldName = chunk.getWorld().getName();
        }

        public Chunk toChunk() {
            return new NonLoadingChunk(x, z, Bukkit.getWorld(worldName));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChunkKey)) return false;
            ChunkKey key = (ChunkKey) o;
            return x == key.x &&
                    z == key.z &&
                    Objects.equals(worldName, key.worldName);
        }

        @Override
        public int hashCode() {

            return Objects.hash(x, z, worldName);
        }

        @Override
        public String toString() {
            return String.format("Chunk at %s,%s", x, z);
        }
    }
}

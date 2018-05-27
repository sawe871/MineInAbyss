package com.derongan.minecraft.mineinabyss.World;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;

import java.util.UUID;

/**
 * Manages special entities on chunks. Special entities are those that we want to be able
 * to spawn/despawn while a chunk is not loaded
 */
public interface EntityChunkManager {
    void loadChunk(Chunk chunk);

    void unloadChunk(Chunk chunk);

    void addEntity(Chunk chunk, ChunkEntity chunkEntity);

    void removeEntity(Chunk chunk, Entity entity);

    /**
     * Checks if an entity is registered as a removable entity.
     * The entity must be on a loaded chunk.
     * @param uuid The UUID of the entity
     */
    boolean isEntityRegistered(UUID uuid);
}

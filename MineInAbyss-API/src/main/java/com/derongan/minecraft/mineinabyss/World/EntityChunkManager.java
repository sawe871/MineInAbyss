package com.derongan.minecraft.mineinabyss.World;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;

import java.util.UUID;

/**
 * Manages special entities on chunks. Special entities are those that we want to be able
 * to spawn/despawn while a chunk is not loaded. This manager is responsible for removing expired
 * entities on chunk load, as well as periodically.
 */
public interface EntityChunkManager {
    /**
     * Loads a chunks entities. If the entities are expired
     * removes them.
     *
     * @param chunk The chunk to load
     */
    void loadChunk(Chunk chunk);

    /**
     * Unloads a chunks entities
     *
     * @param chunk the chunk to unload
     */
    void unloadChunk(Chunk chunk);

    /**
     * Add a chunk entity to the chunk.
     * Also spawns the entity if the chunk is loaded.
     *
     * @param chunk The chunk to add to
     * @param chunkEntity The chunk entity to add
     */
    void addEntity(Chunk chunk, ChunkEntity chunkEntity);

    /**
     * Removes an entity from a chunk. You can only remove
     * entities if a chunk is loaded. Otherwise you
     * should rely on timed expiration to remove them.
     *
     * @param chunk The chunk to remove from
     * @param entity The real entity that is being removed.
     */
    void removeEntity(Chunk chunk, Entity entity);

    /**
     * Checks if an entity is registered as a removable entity.
     * The entity must be on a loaded chunk.
     *
     * @param uuid The UUID of the entity
     */
    boolean isEntityRegistered(UUID uuid);


    /**
     * Check if an entity exists at coordinates on a chunk
     *
     * @param chunk The chunk to check.
     * @param x The x coordinate of the entity.
     * @param y The y coordinate of teh entity
     * @param z The z coordinate of the entity
     * @return The true if there is an entity, false otherwise.
     */
    boolean isEntityAt(Chunk chunk, int x, int y, int z);

    /**
     * Unloads all entities
     */
    void disable();
}

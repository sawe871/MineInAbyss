package com.derongan.minecraft.mineinabyss.World;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;

/**
 * A chunk we can construct simply to hold location and world
 */
public class NonLoadingChunk implements Chunk {
    private int x;
    private int z;
    private World world;

    public NonLoadingChunk(int x, int z, World world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Block getBlock(int i, int i1, int i2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChunkSnapshot getChunkSnapshot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChunkSnapshot getChunkSnapshot(boolean b, boolean b1, boolean b2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity[] getEntities() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockState[] getTileEntities() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public boolean load(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean load() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unload(boolean b, boolean b1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unload(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unload() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSlimeChunk() {
        throw new UnsupportedOperationException();
    }
}

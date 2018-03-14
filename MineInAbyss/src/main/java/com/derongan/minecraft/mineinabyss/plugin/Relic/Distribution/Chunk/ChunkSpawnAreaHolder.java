package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk;

import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.SpawnArea;

import java.util.List;

public class ChunkSpawnAreaHolder {
    private int chunkX;
    private int chunkZ;

    private List<SpawnArea> spawnAreas;


    public ChunkSpawnAreaHolder(int chunkX, int chunkZ, List<SpawnArea> spawnAreas) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.spawnAreas = spawnAreas;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }


    public List<SpawnArea> getSpawnAreas() {
        return spawnAreas;
    }
}

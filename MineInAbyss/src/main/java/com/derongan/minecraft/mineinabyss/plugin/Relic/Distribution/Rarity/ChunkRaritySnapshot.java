package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Rarity;

import org.bukkit.ChunkSnapshot;

public class ChunkRaritySnapshot {
    private long[][] rarities = new long[256][16];
    private ChunkSnapshot snapshot;
    private int x;
    private int z;

    public ChunkRaritySnapshot(ChunkSnapshot snapshot, int x, int z, RaritySetter raritySetter) {
        for (int cx = 0; cx < 16; cx++) {
            for (int cy = 0; cy < 256; cy++) {
                for (int cz = 0; cz < 16; cz++) {
                    this.setRarity(cx, cy, cz, raritySetter.rarity(snapshot, cx, cy, cz));
                }
            }
        }

        this.snapshot = snapshot;

        this.x = x;
        this.z = z;
    }

    public int getRarity(int x, int y, int z) {
        if(x > 15 || x  < 0 || z > 15 || z < 0 || y < 0 || y > 255)
            return 0;
        return (int) ((rarities[y][x] & (0b1111L << ((long)z*4L))) >> ((long)z*4L));
    }

    private void setRarity(int x, int y, int z, int rarity) {
        rarities[y][x] ^= (long)rarity << (z*4L);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public ChunkSnapshot getSnapshot() {
        return snapshot;
    }

    @FunctionalInterface
    public interface RaritySetter {
        int rarity(ChunkSnapshot snapshot, int x, int y, int z);
    }
}

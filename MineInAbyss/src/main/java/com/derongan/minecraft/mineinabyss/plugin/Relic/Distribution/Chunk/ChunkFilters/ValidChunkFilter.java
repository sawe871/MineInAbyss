package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk.ChunkFilters;

import org.bukkit.ChunkSnapshot;

import java.util.function.Predicate;

public class ValidChunkFilter implements Predicate<ChunkSnapshot>{
    @Override
    public boolean test(ChunkSnapshot snapshot) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int highest = snapshot.getHighestBlockYAt(x, z);
                if (highest > 10) {
                    return true;
                }
            }
        }
        return false;
    }
}

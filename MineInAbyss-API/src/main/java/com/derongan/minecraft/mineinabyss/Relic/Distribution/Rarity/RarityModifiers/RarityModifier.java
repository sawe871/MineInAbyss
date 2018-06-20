package com.derongan.minecraft.mineinabyss.Relic.Distribution.Rarity.RarityModifiers;

import org.bukkit.ChunkSnapshot;

/**
 * Interface that defines operations on a chunk snapshot location to determine its rarity
 */
@FunctionalInterface
public interface RarityModifier {
    double modify(int x, int y, int z, ChunkSnapshot snapshot, double initial);
}

package com.derongan.minecraft.mineinabyss.Relic.Distribution.Rarity.RarityModifiers;

import org.bukkit.ChunkSnapshot;

public class LightingRarityModifier implements RarityModifier {
    @Override
    public double modify(int x, int y, int z, ChunkSnapshot snapshot, double initial) {
        return initial * (1 - (snapshot.getBlockSkyLight(x, y, z) / 25.0));
    }
}

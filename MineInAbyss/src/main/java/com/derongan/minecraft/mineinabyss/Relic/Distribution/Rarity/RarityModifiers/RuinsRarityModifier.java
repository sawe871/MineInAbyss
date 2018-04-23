package com.derongan.minecraft.mineinabyss.Relic.Distribution.Rarity.RarityModifiers;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;

import java.util.Random;

public class RuinsRarityModifier implements RarityModifier {
    // Force a random with a constant seed so we always generate the same regions
    Random random = new Random(0L);

    @Override
    public double modify(int x, int y, int z, ChunkSnapshot snapshot, double initial) {
        Material belowMat = y > 0 ? snapshot.getBlockType(x, y - 1, z) : Material.AIR;

        if(belowMat == Material.SMOOTH_BRICK)
            return initial;

        if(belowMat == Material.GRASS || belowMat == Material.DIRT)
            initial *= .5;

        initial *= random.nextDouble();

        return .3 * initial;
    }
}

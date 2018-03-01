package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Rarity.RarityModifiers;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;

public class InvalidRarityModifier implements RarityModifier {
    @Override
    public double modify(int x, int y, int z, ChunkSnapshot snapshot, double initial) {
        Material belowMat = y > 0 ? snapshot.getBlockType(x, y - 1, z) : Material.AIR;
        Material blockMat = snapshot.getBlockType(x, y, z);

        if (!belowMat.isOccluding() || blockMat != Material.AIR)
            return 0;

        return initial;
    }
}

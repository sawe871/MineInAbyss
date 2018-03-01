package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Rarity.RarityModifiers;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;

public class BackingRarityModifier implements RarityModifier{
    @Override
    public double modify(int x, int y, int z, ChunkSnapshot snapshot, double initial) {
        boolean north = x < 15 && snapshot.getBlockType(x + 1, y, z) != Material.AIR;
        boolean south = x > 0 && snapshot.getBlockType(x - 1, y, z) != Material.AIR;
        boolean east = z < 15 && snapshot.getBlockType(x, y, z + 1) != Material.AIR;
        boolean west = z > 0 && snapshot.getBlockType(x, y, z - 1) != Material.AIR;

        int count = (north ? 1 : 0) + (south ? 1 : 0) + (east ? 1 : 0) + (west ? 1 : 0);

        switch (count) {
            case 0:
                initial *= .5;
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                initial *= .75;
                break;
            case 4:
                break;
        }

        return initial;
    }
}

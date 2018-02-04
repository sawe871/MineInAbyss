package com.derongan.minecraft.mineinabyss.plugin.Relic.Looting.Filters;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;

import java.util.function.Predicate;

public class EnoughRoomFilter implements Predicate<ChunkSnapshot> {
    @Override
    public boolean test(ChunkSnapshot snapshot) {
        for (int i = 1; i < 32; i++) {
            boolean hasAir = snapshot.getBlockType(0, i * 8 - 4, 8) == Material.AIR;
            hasAir = hasAir || snapshot.getBlockType(8, i * 8 - 4, 8) == Material.AIR;
            hasAir = hasAir || snapshot.getBlockType(16, i * 8 - 4, 8) == Material.AIR;
            hasAir = hasAir || snapshot.getBlockType(8, i * 8 - 4, 0) == Material.AIR;
            hasAir = hasAir || snapshot.getBlockType(8, i * 8 - 4, 16) == Material.AIR;


            if (hasAir) {
                return true;
            }
        }
        return false;
    }
}

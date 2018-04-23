package com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.util.function.Supplier;

public class ChunkSupplier implements Supplier<ChunkSnapshot> {
    private final int xhigh;
    private final int xlow;
    private final int zhigh;
    private final int zlow;

    private int currentX;
    private int currentZ;

    private World world;

    public ChunkSupplier(Point topRight, Point bottomLeft, World world) {
        xhigh = Math.max(topRight.x, bottomLeft.x);
        xlow = Math.min(topRight.x, bottomLeft.x);
        zlow = Math.min(topRight.z, bottomLeft.z);
        zhigh = Math.max(topRight.z, bottomLeft.z);

        currentX = xlow;
        currentZ = zlow;

        this.world = world;
    }

    public int getNumberOfChunks() {
        return Math.abs((xhigh - xlow) * (zhigh - zlow));
    }

    @Override
    public ChunkSnapshot get() {
        if (currentZ >= zhigh) {
            return null;
        }
        if (currentX >= xhigh) {
            currentX = xlow;
            currentZ++;
        }

        world.loadChunk(currentX, currentZ);
        Chunk chunk = world.getChunkAt(currentX, currentZ);

        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof ArmorStand && !((ArmorStand) entity).isVisible()) {
                entity.remove();
            }
        }
        ChunkSnapshot snapshot = chunk.getChunkSnapshot();
        chunk.unload(true);

        currentX++;

        return snapshot;
    }
}

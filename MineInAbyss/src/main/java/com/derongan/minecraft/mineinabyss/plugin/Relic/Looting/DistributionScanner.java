package com.derongan.minecraft.mineinabyss.plugin.Relic.Looting;

import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Looting.Filters.EnoughRoomFilter;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Looting.Filters.ValidChunkFilter;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DistributionScanner {
    private World world;
    private AbyssContext context;

    public DistributionScanner(World world, AbyssContext context) {
        this.world = world;
        this.context = context;
    }

    public List<ChunkSnapshot> scan(Point topRight, Point bottomLeft) {
        ChunkSupplier supplier = new ChunkSupplier(topRight, bottomLeft);
        Stream<ChunkSnapshot> chunks = Stream.generate(supplier);

        context.getLogger().info(String.format("%d chunks at start", supplier.getNumberOfChunks()));

        List<ChunkSnapshot> filtered = chunks
                .limit(supplier.getNumberOfChunks())
                .filter(Objects::nonNull)
                .filter(new ValidChunkFilter())
                .filter(new EnoughRoomFilter())
                .collect(Collectors.toList());

        context.getLogger().info(String.format("%d chunks after filter", filtered.size()));

        return filtered;
    }

    private class ChunkSupplier implements Supplier<ChunkSnapshot> {
        private final int xhigh;
        private final int xlow;
        private final int zhigh;
        private final int zlow;

        private int currentX;
        private int currentZ;

        private boolean done = false;

        ChunkSupplier(Point topRight, Point bottomLeft) {
            xhigh = topRight.x;
            xlow = bottomLeft.x;
            zlow = topRight.y;
            zhigh = bottomLeft.y;

            currentX = xlow;
            currentZ = zlow;
        }

        int getNumberOfChunks(){
            return (xhigh-xlow-1)*(zhigh-zlow-1);
        }

        @Override
        public ChunkSnapshot get() {
            if (currentX >= xhigh) {
                currentX = xlow;
                currentZ++;
            }

            if (currentZ >= zhigh) {
                done = true;
                return null;
            }

            Chunk chunk = world.getChunkAt(currentX, currentZ);
            for (Entity entity : chunk.getEntities()) {
                if(entity instanceof ArmorStand && !((ArmorStand) entity).isVisible()){
                    entity.remove();
                }
            }
            ChunkSnapshot snapshot = chunk.getChunkSnapshot();
            chunk.unload(true);

            currentX++;

            return snapshot;
        }
    }
}

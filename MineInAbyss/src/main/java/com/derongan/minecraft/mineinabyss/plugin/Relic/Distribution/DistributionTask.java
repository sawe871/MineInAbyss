package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Relics.LootableRelicType;
import com.derongan.minecraft.mineinabyss.plugin.TickUtils;
//import org.apache.commons.math3.distribution.BetaDistribution;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

public class DistributionTask implements Runnable {
    private AbyssContext context;
    private LootableRelicType lootableRelicType;
    private World world;

    private Random random = new Random();

    public DistributionTask(AbyssContext context, World world) {
        this.context = context;
        lootableRelicType = new LootableRelicType();

        this.world = world;
    }

    @Override
    public void run() {
        if (context.getLayerMap().get(world.getName()).ready) {
            for (Player player : world.getPlayers()) {
                int px = player.getLocation().getChunk().getX();
                int pz = player.getLocation().getChunk().getZ();
                for (int x = -2; x < 3; x++) {
                    for (int z = -2; z < 3; z++) {
                        Point key = new Point(x + px, 0, z + pz);
                        ChunkSpawnAreaHolder holder = context.getSpawnAreas(world.getName() + "/section_0", key);

                        holder.getSpawnAreas().forEach(SpawnArea::displayRegion);
                    }
                }
            }
        }

//        ChunkSpawnAreaHolder holder = context.getSpawnAreas(world.getName() + "/section_0", key) .get(random.nextInt(holders.size()));
//
//        if(holder.getSpawnAreas().size() > 0) {
//            SpawnArea spawnArea = holder.getSpawnAreas().get(random.nextInt(holder.getSpawnAreas().size()));
//
//            Point point = spawnArea.getRandomPoint();
//
//            Vector vector = new Vector(point.x, point.y, point.z);
//
//            spawnLootableRelic(vector.toLocation(world), randomRelicType());
    }

    public RelicType randomRelicType() {
        Object[] relicTypes = RelicType.registeredRelics.values().toArray();
        return (RelicType) relicTypes[random.nextInt(relicTypes.length)];
    }

    void spawnLootableRelic(Location location, RelicType relicType) {
        lootableRelicType.spawnLootableRelic(location, relicType, TickUtils.milisecondsToTicks(10000000));
    }
}

package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Relics.LootableRelicType;
import com.derongan.minecraft.mineinabyss.plugin.TickUtils;
//import org.apache.commons.math3.distribution.BetaDistribution;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class DistributionTask implements Runnable {
    private AbyssContext context;
    private LootableRelicType lootableRelicType;
    private World world;

    private List<ChunkSpawnAreaHolder> holders;

    private Random random = new Random();

    public DistributionTask(AbyssContext context, World world, List<ChunkSpawnAreaHolder> holders) {
        this.context = context;
        lootableRelicType = new LootableRelicType();

        this.world = world;

        this.holders = holders;
    }

    @Override
    public void run() {
        for (ChunkSpawnAreaHolder holder : holders) {
            boolean visible = false;

            int cx = holder.getChunkX();
            int cz = holder.getChunkZ();

            Vector spawnChunkVec = new Vector(cx,0,cz);

            for (Player player : world.getPlayers()) {
                int x = player.getLocation().getChunk().getX();
                int z = player.getLocation().getChunk().getZ();
                Vector playerChunkVec = new Vector(x,0,z);
                if(spawnChunkVec.distance(playerChunkVec) < 4)
                    visible = true;
            }
            if(visible)
                holder.getSpawnAreas().forEach(SpawnArea::displayRegion);
        }

        ChunkSpawnAreaHolder holder = holders.get(random.nextInt(holders.size()));

        if(holder.getSpawnAreas().size() > 0) {
            SpawnArea spawnArea = holder.getSpawnAreas().get(random.nextInt(holder.getSpawnAreas().size()));

            Point point = spawnArea.getRandomPoint();

            Vector vector = new Vector(point.x, point.y, point.z);

            spawnLootableRelic(vector.toLocation(world), randomRelicType());
        }
    }

    public RelicType randomRelicType() {
        Object[] relicTypes = RelicType.registeredRelics.values().toArray();
        return (RelicType) relicTypes[random.nextInt(relicTypes.length)];
    }

    void spawnLootableRelic(Location location, RelicType relicType) {
        lootableRelicType.spawnLootableRelic(location, relicType, TickUtils.milisecondsToTicks(10000000));
    }
}

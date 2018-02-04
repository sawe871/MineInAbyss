package com.derongan.minecraft.mineinabyss.plugin.Relic.Looting;

import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Relics.LootableRelicType;
import com.derongan.minecraft.mineinabyss.plugin.TickUtils;
//import org.apache.commons.math3.distribution.BetaDistribution;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class DistributionTask extends BukkitRunnable {
    private AbyssContext context;
    private LootableRelicType lootableRelicType;
    private World world;

    private List<ChunkSnapshot> validChunks;

    private Random random = new Random();

    public DistributionTask(AbyssContext context, World world, List<ChunkSnapshot> validChunks) {
        this.context = context;
        lootableRelicType = new LootableRelicType();

        this.world = world;

        this.validChunks = validChunks;
    }

    @Override
    public void run() {
        chooseSpawnLocation(randomChunk());
    }

    private ChunkSnapshot randomChunk(){
        return validChunks.get(random.nextInt(validChunks.size()));
    }

    private void chooseSpawnLocation(ChunkSnapshot snapshot) {
        int xStart = snapshot.getX();
        int zStart = snapshot.getZ();
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int y = random.nextInt(256);

            if (y < 1 || y > 256) {
                continue;
            }

            Material blockType = snapshot.getBlockType(x, y, z);
            Material blockBelow = snapshot.getBlockType(x, y - 1, z);

            if (blockType == Material.AIR && blockBelow != Material.AIR && blockBelow != Material.WATER && blockBelow != Material.STATIONARY_WATER) {
                Location location = new Location(world, 16 * xStart + x, y, 16 * zStart + z);
                if (!world.getNearbyEntities(location, 1, 1, 1).stream().anyMatch(a -> a instanceof ArmorStand)) {
                    spawnLootableRelic(location, randomRelicType());
                    System.out.println(String.format("Spawned relic @ %d %d %d", 16 * xStart + x, y, 16 * zStart + z));
                    return;
                }
            }
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

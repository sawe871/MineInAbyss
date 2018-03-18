package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Layer;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk.Point;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Serialization.LootSerializationManager;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Relics.LootableRelicType;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Relics.StandardRelicType;
import com.derongan.minecraft.mineinabyss.plugin.TickUtils;
import com.google.common.cache.LoadingCache;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;
import java.util.Random;

public class DistributionTask extends BukkitRunnable {
    private AbyssContext context;
    private LootableRelicType lootableRelicType;
    private World world;

    private Map<String, String> sections;

    private Random random = new Random();

    private boolean shouldSchedule = true;

    public DistributionTask(AbyssContext context, World world) {
        this.context = context;
        this.world = world;

        lootableRelicType = new LootableRelicType();

        Path filePath = context.getPlugin().getDataFolder().toPath().resolve("distribution").resolve(world.getName());

        Layer layer = context.getLayerMap().get(world.getName());

        // Cancel if world is not abyss or has no data
        if (layer == null || !filePath.toFile().exists()) {
            shouldSchedule = false;
        } else {
            String[] availSections = filePath.toFile().list((current, name) -> new File(current, name).isDirectory());

            if(availSections == null || availSections.length == 0){
                shouldSchedule = false;
            }


        }

        context.getLogger().info(String.format("Loot for %s: %b", world.getName(), shouldSchedule));
    }

    public boolean shouldSchedule(){
        return shouldSchedule;
    }

    @Override
    public void run() {
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

        Path filePath = context.getPlugin().getDataFolder().toPath().resolve("distribution");
        filePath = filePath.resolve(world.getName() + "/section_0");

        File[] chunkFiles = filePath.toFile().listFiles();

        if (chunkFiles.length == 0)
            return;

        File chosen = chunkFiles[random.nextInt(chunkFiles.length)];

        int chunkX = Integer.valueOf(chosen.getName().split("_")[0]);
        int chunkZ = Integer.valueOf(chosen.getName().split("_")[1].split("\\.")[0]);

        LootSerializationManager someManager = new LootSerializationManager(filePath.toString(), context);

        Reader reader;
        try {
            reader = new FileReader(filePath.resolve(chosen.getName()).toFile());
        } catch (FileNotFoundException e) {
            return;
        }

        ChunkSpawnAreaHolder holder = new ChunkSpawnAreaHolder(chunkX, chunkZ, someManager.deserializeChunk(reader));


        if (holder.getSpawnAreas().size() > 0) {
            SpawnArea spawnArea = holder.getSpawnAreas().get(random.nextInt(holder.getSpawnAreas().size()));

            Point point = spawnArea.getRandomPoint();

            Vector vector = new Vector(point.x, point.y, point.z);

            spawnLootableRelic(vector.toLocation(world), randomRelicType());
        }
    }

    public RelicType randomRelicType() {
        return StandardRelicType.ROPE_LADDER;
//        Object[] relicTypes = RelicType.registeredRelics.values().toArray();
//        return (RelicType) relicTypes[random.nextInt(relicTypes.length)];
    }

    void spawnLootableRelic(Location location, RelicType relicType) {
        lootableRelicType.spawnLootableRelic(location, relicType, TickUtils.milisecondsToTicks(10000000));
    }
}

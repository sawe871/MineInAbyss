package com.derongan.minecraft.mineinabyss.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.Relic.Relics.LootableRelicType;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.util.TickUtils;
import org.bukkit.Location;
import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Layer;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk.Point;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Serialization.LootSerializationManager;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Relics.LootableRelicType;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Relics.StandardRelicType;
import com.derongan.minecraft.mineinabyss.plugin.TickUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DistributionTask extends BukkitRunnable {
    private AbyssContext context;
    private LootableRelicType lootableRelicType;
    private World world;

    private Map<String, LoadingCache<Point, ChunkSpawnAreaHolder>> sections;

    private Map<String, List<Point>> sectionLootChunks;

    private Random random = new Random();

    //#TODO this is temp
    private List<RelicType> acceptable = Arrays.asList(
            StandardRelicType.ROPE_LADDER,
            StandardRelicType.BLAZE_REAP,
            StandardRelicType.PUSHER,
            StandardRelicType.THOUSAND_MEN_PINS
    );

    private boolean shouldSchedule = true;

    public DistributionTask(AbyssContext context, World world) {
        this.context = context;
        this.world = world;
        this.sections = new HashMap<>();
        this.sectionLootChunks = new HashMap<>();

        lootableRelicType = new LootableRelicType();

        Path filePath = context.getPlugin().getDataFolder().toPath().resolve("distribution").resolve(world.getName());

        Layer layer = context.getLayerMap().get(world.getName());

        // Cancel if world is not abyss or has no data
        if (layer == null || !filePath.toFile().exists()) {
            shouldSchedule = false;
        } else {
            String[] availSections = filePath.toFile().list((current, name) -> new File(current, name).isDirectory());

            if (availSections == null || availSections.length == 0) {
                shouldSchedule = false;
            } else {
                Arrays.stream(availSections).forEach(a -> {
                    sections.put(a, CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build(
                            new SpawnAreaHolderCacheLoader(filePath.resolve(a).toString())
                    ));

                    List<Point> points = Arrays.stream(filePath.resolve(a).toFile().listFiles()).map(f->{
                        int chunkX = Integer.valueOf(f.getName().split("_")[0]);
                        int chunkZ = Integer.valueOf(f.getName().split("_")[1].split("\\.")[0]);

                        return new Point(chunkX, 0, chunkZ);
                    }).collect(Collectors.toList());
                    sectionLootChunks.put(a, points);
                });
            }
        }

        context.getLogger().info(String.format("Loot for %s: %b", world.getName(), shouldSchedule));
    }

    public class SpawnAreaHolderCacheLoader extends CacheLoader<Point, ChunkSpawnAreaHolder> {
        LootSerializationManager manager;

        public SpawnAreaHolderCacheLoader(String path) {
            this.manager = new LootSerializationManager(path, context);
        }

        @Override
        public ChunkSpawnAreaHolder load(Point point) throws Exception {
            Reader reader;
            try {
                reader = new FileReader(manager.chunkToPath(point.x, point.z).toFile());
            } catch (FileNotFoundException e) {
                return new ChunkSpawnAreaHolder(point.x, point.z, Collections.emptyList());
            }

            return new ChunkSpawnAreaHolder(point.x, point.z, manager.deserializeChunk(reader));
        }
    }


    public boolean shouldSchedule() {
        return shouldSchedule;
    }

    @Override
    public void run() {
        sections.keySet().forEach(sectionName -> {
            for (Player player : world.getPlayers()) {
                if (context.getPlayerAcensionDataMap().get(player.getUniqueId()).isDev()) {
                    int px = player.getLocation().getChunk().getX();
                    int pz = player.getLocation().getChunk().getZ();
                    for (int x = -2; x < 3; x++) {
                        for (int z = -2; z < 3; z++) {
                            Point key = new Point(x + px, 0, z + pz);
                            ChunkSpawnAreaHolder holder = null;
                            try {
                                holder = sections.get(sectionName).get(key);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            holder.getSpawnAreas().forEach(SpawnArea::displayRegion);
                        }
                    }
                }
            }

            if(sectionLootChunks.get(sectionName).size() == 0)
                return;

            Point chosen = sectionLootChunks.get(sectionName).get(random.nextInt(sectionLootChunks.get(sectionName).size()));

            ChunkSpawnAreaHolder holder = null;
            try {
                holder = sections.get(sectionName).get(chosen);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (holder.getSpawnAreas().size() > 0) {
                SpawnArea spawnArea = holder.getSpawnAreas().get(random.nextInt(holder.getSpawnAreas().size()));

                Point point = spawnArea.getRandomPoint();

                Vector vector = new Vector(point.x, point.y, point.z);

                spawnLootableRelic(vector.toLocation(world), randomRelicType());
            }
        });
    }

    public RelicType randomRelicType() {
        return acceptable.get(random.nextInt(acceptable.size()));
//        Object[] relicTypes = RelicType.registeredRelics.values().toArray();
//        return (RelicType) relicTypes[random.nextInt(relicTypes.length)];
    }

    void spawnLootableRelic(Location location, RelicType relicType) {
        location.getChunk().load();
        lootableRelicType.spawnLootableRelic(location, relicType, TickUtils.milisecondsToTicks(300000));
    }
}

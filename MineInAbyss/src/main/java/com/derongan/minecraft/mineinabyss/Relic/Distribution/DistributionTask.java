package com.derongan.minecraft.mineinabyss.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.Relic.RelicGroundEntity;
import com.derongan.minecraft.mineinabyss.World.AbyssWorldManager;
import com.derongan.minecraft.mineinabyss.World.Point;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Serialization.LootSerializationManager;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.Relic.Relics.StandardRelicType;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

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

        Path filePath = context.getPlugin().getDataFolder().toPath().resolve("distribution").resolve("section_1");

        // Cancel if world is not abyss or has no data
        if (!filePath.toFile().exists()) {
            shouldSchedule = false;
        } else {
            sections.put("section_1", CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build(
                    new SpawnAreaHolderCacheLoader(filePath.toString())
            ));

            List<Point> points = Arrays.stream(filePath.toFile().listFiles()).map(f -> {
                int chunkX = Integer.valueOf(f.getName().split("_")[0]);
                int chunkZ = Integer.valueOf(f.getName().split("_")[1].split("\\.")[0]);

                return new Point(chunkX, 0, chunkZ);
            }).collect(Collectors.toList());
            sectionLootChunks.put("section_1", points);
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
                if (context.getPlayerDataMap().get(player.getUniqueId()).canSeeLootSpawns()) {
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

            if (sectionLootChunks.get(sectionName).size() == 0)
                return;

            Point chosen = sectionLootChunks.get(sectionName).get(random.nextInt(sectionLootChunks.get(sectionName).size()));

            ChunkSpawnAreaHolder holder = null;
            try {
                holder = sections.get(sectionName).get(chosen);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (holder.getSpawnAreas() != null && holder.getSpawnAreas().size() > 0) {
                SpawnArea spawnArea = holder.getSpawnAreas().get(random.nextInt(holder.getSpawnAreas().size()));

                Point point = spawnArea.getRandomPoint();

                Vector vector = new Vector(point.x, point.y, point.z);

                spawnLootableRelic(vector.toLocation(world), randomRelicType());
            }
        });
    }

    public RelicType randomRelicType() {
        return acceptable.get(random.nextInt(acceptable.size()));
    }

    private void spawnLootableRelic(Location location, RelicType relicType) {
        context.getEntityChunkManager().addEntity(
                location.getChunk(),
                new RelicGroundEntity(relicType, location.getBlockX(), location.getBlockY(), location.getBlockZ())
        );
    }
}

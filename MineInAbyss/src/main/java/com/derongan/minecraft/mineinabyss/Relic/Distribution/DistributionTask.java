package com.derongan.minecraft.mineinabyss.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Player.PlayerData;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Serialization.LootSerializationManager;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.SpawnArea;
import com.derongan.minecraft.mineinabyss.Relic.RelicGroundEntity;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.Relic.Relics.StandardRelicType;
import com.derongan.minecraft.mineinabyss.World.*;
import com.derongan.minecraft.mineinabyss.util.TickUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//new dist task
public class DistributionTask extends BukkitRunnable {
    private AbyssContext context;
    private AbyssWorldManager worldManager;
    private EntityChunkManager chunkManager;

    private Layer layer;

    private Random random;

    private LoadingCache<String, ChunkSpawnAreaHolder> cachedSpawnAreas;

    // Map of sections to available spawn files.
    private List<String> spawnFiles;


    //#TODO this is temp
    private List<RelicType> acceptable = Arrays.asList(
            StandardRelicType.ROPE_LADDER,
            StandardRelicType.BLAZE_REAP,
            StandardRelicType.PUSHER,
            StandardRelicType.THOUSAND_MEN_PINS
    );

    public DistributionTask(AbyssContext context, Layer layer) {
        this.context = context;
        this.worldManager = context.getWorldManager();
        this.chunkManager = context.getEntityChunkManager();

        this.layer = layer;

        this.random = new Random();

        this.spawnFiles = layer.getSections()
                .stream()
                .flatMap(this::getFileList)
                .collect(Collectors.toList());


        //TODO do we need to fix this outdir stuff?
        LootSerializationManager lootSerializationManager = new LootSerializationManager("", context);

        cachedSpawnAreas = CacheBuilder.newBuilder().build(new CacheLoader<String, ChunkSpawnAreaHolder>() {
            @Override
            public ChunkSpawnAreaHolder load(String stringPath) throws Exception {
                Reader reader;

                String filename = stringPath.split(File.separator)[stringPath.split(File.separator).length - 1];

                //TODO THIS IS SUPER HACKY AND BAD
                int x = Integer.valueOf(filename.split("_")[0]);
                int y = Integer.valueOf(filename.split("_")[1].split("\\.")[0]);

                try {
                    reader = new FileReader(stringPath);
                } catch (FileNotFoundException e) {
                    return new ChunkSpawnAreaHolder(x, y, Collections.emptyList());
                }

                return new ChunkSpawnAreaHolder(x, y, lootSerializationManager.deserializeChunk(reader));
            }
        });

        Bukkit.getScheduler().scheduleSyncRepeatingTask(context.getPlugin(), () -> {
            context.getPlayerDataMap().values().stream().filter(PlayerData::canSeeLootSpawns).forEach(a -> {
                displayLootSpawn(a, lootSerializationManager);
            });

        }, 0, TickUtils.milisecondsToTicks(1500));
    }

    private void displayLootSpawn(PlayerData p, LootSerializationManager lootSerializationManager) {
        Chunk chunk = p.getPlayer().getLocation().getChunk();


        //TODO HACKY
        Path path = context.getPlugin()
                .getDataFolder()
                .toPath()
                .resolve("distribution")
                .resolve("section_" + (p.getCurrentSection().getIndex()));

        path = path.resolve(lootSerializationManager.chunkToPath(chunk.getX(), chunk.getZ()));

        ChunkSpawnAreaHolder holder = cachedSpawnAreas.getIfPresent(path.toString());

        if (holder == null)
            return;

        int i = 0;

        for (SpawnArea a : holder.getSpawnAreas()) {
            double scaled = i / (double)holder.getSpawnAreas().size();
            for (Point point : a.getBlocks()) {
                p.getPlayer().spawnParticle(
                        Particle.REDSTONE,
                        point.getX() + .5,
                        point.getY() + .2,
                        point.getZ() + .5,
                        0, 0.001 + scaled, 1 - scaled, 0, 1);
            }
            i++;
        }
    }

    private Stream<String> getFileList(Section section) {
        Path filePath = context.getPlugin()
                .getDataFolder()
                .toPath()
                .resolve("distribution")
                .resolve("section_" + (section.getIndex()));

        // Check if the path exists
        if (filePath.toFile().exists()) {
            return Arrays.stream(filePath.toFile().listFiles()).map(File::getPath);
        } else {
            return Stream.empty();
        }
    }

    @Override
    public void run() {
        if (spawnFiles.size() > 0) {
            String randomPath = spawnFiles.get(random.nextInt(spawnFiles.size()));
            try {
                ChunkSpawnAreaHolder holder = cachedSpawnAreas.get(randomPath);
                SpawnArea spawnArea = holder.getSpawnAreas().get(random.nextInt(holder.getSpawnAreas().size()));

                //Random spawn point
                Point point = spawnArea.getRandomPoint();

                Chunk chunk;

                World world = Bukkit.getWorld(spawnArea.getWorldName());
                if (!world.isChunkLoaded(holder.getChunkX(), holder.getChunkZ())) {
                    chunk = new NonLoadingChunk(holder.getChunkX(), holder.getChunkZ(), world);
                } else {
                    chunk = world.getChunkAt(holder.getChunkX(), holder.getChunkZ());
                }

                long inArea = spawnArea.getBlocks().stream().filter(a -> chunkManager.isEntityAt(chunk, a.getX(), a.getY(), a.getZ())).count();

                if (inArea < 2 && !chunkManager.isEntityAt(chunk, point.getX(), point.getY(), point.getZ())) {
                    chunkManager.addEntity(chunk, new RelicGroundEntity(acceptable.get(random.nextInt(acceptable.size())), point.getX(), point.getY(), point.getZ(), TimeUnit.MINUTES.toMillis(20)));
                }
            } catch (ExecutionException e) {
                context.getLogger().warning("Problem getting entry for loot cache");
            }
        }
    }
}

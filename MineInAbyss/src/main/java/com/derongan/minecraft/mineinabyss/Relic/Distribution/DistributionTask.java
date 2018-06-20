package com.derongan.minecraft.mineinabyss.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Serialization.LootSerializationManager;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.SpawnArea;
import com.derongan.minecraft.mineinabyss.Relic.RelicGroundEntity;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.Relic.Relics.StandardRelicType;
import com.derongan.minecraft.mineinabyss.World.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
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
        LootSerializationManager lootSerializationManager = new LootSerializationManager("",context);

        cachedSpawnAreas = CacheBuilder.newBuilder().build(new CacheLoader<String, ChunkSpawnAreaHolder>() {
            @Override
            public ChunkSpawnAreaHolder load(String stringPath) throws Exception {
                Reader reader;

                String filename = stringPath.split("/")[stringPath.split("/").length-1];

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
    }

    private Stream<String> getFileList(Section section){
        Path filePath = context.getPlugin()
                .getDataFolder()
                .toPath()
                .resolve("distribution")
                .resolve("section_" + (section.getIndex()));

        // Check if the path exists
        if(filePath.toFile().exists()){
            return Arrays.stream(filePath.toFile().listFiles()).map(File::getPath);
        } else {
            return Stream.empty();
        }
    }

    @Override
    public void run() {
        if(spawnFiles.size() > 0) {
            String randomPath = spawnFiles.get(random.nextInt(spawnFiles.size()));
            try {
                ChunkSpawnAreaHolder holder = cachedSpawnAreas.get(randomPath);
                SpawnArea spawnArea = holder.getSpawnAreas().get(random.nextInt(holder.getSpawnAreas().size()));

                //Random spawn point
                Point point = spawnArea.getRandomPoint();

                Chunk chunk = Bukkit.getWorld(spawnArea.getWorldName()).getChunkAt(holder.getChunkX(), holder.getChunkZ());

                if (!chunkManager.isEntityAt(chunk, point.getX(), point.getY(), point.getZ())) {
                    chunkManager.addEntity(chunk, new RelicGroundEntity(acceptable.get(random.nextInt(acceptable.size())), point.getX(), point.getY(), point.getZ()));
                }
            } catch (ExecutionException e) {
                context.getLogger().warning("Problem getting entry for loot cache");
            }
        }
    }
}

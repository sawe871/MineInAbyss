package com.derongan.minecraft.mineinabyss.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.ChunkFilters.EnoughRoomFilter;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.ChunkFilters.ValidChunkFilter;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Rarity.ChunkRaritySnapshot;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Rarity.RarityModifiers.*;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.ChunkSupplier;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.Point;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Rarity.RarityModifiers.*;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Serialization.LootSerializationManager;
import com.derongan.minecraft.mineinabyss.util.TickUtils;
import com.google.common.collect.Iterators;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;

import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DistributionScanner {
    private World world;
    private AbyssContext context;

    private final List<RarityModifier> modifiers = Arrays.asList(
            new InvalidRarityModifier(),
            new LightingRarityModifier(),
            new BackingRarityModifier(),
            new RuinsRarityModifier()
    );

    public DistributionScanner(World world, AbyssContext context) {
        this.world = world;
        this.context = context;
    }

    /**
     * Generate config file for each chunk containing the list of spawn points in a chunk.
     * Do nothing if yaml already exists.
     *
     * @param top    Topmost point
     * @param bottom    Bottommost point
     * @param path    the directory to save to
     */
    public void scan(Point top, Point bottom, Path path, int section) {
        ChunkSupplier supplier = new ChunkSupplier(top, bottom, world);
        Stream<ChunkSnapshot> chunks = Stream.generate(supplier).limit(supplier.getNumberOfChunks());
        LootSerializationManager manager = new LootSerializationManager(path.normalize().toString(), context);

        Stream<ChunkSnapshot> filtered = doFilterChunks(chunks);

        Iterator<List<ChunkSnapshot>> iterator = Iterators.partition(filtered.iterator(), 100);

        doNextChunk(iterator, manager, section);
    }

    // TODO use streams?
    private void doNextChunk(Iterator<List<ChunkSnapshot>> snapshotIterator, LootSerializationManager manager, int section){
        BukkitScheduler scheduler = context.getPlugin().getServer().getScheduler();

        if(snapshotIterator.hasNext()) {
            List<ChunkSnapshot> chunkSnapshots = snapshotIterator.next();

            chunkSnapshots.forEach(a->doSerialize(a, manager));

            scheduler.scheduleSyncDelayedTask(context.getPlugin(), ()->doNextChunk(snapshotIterator, manager, section), 2);
        } else{
            Bukkit.broadcastMessage(String.format("Finished generating %s section %d", world.getName(), section));
        }
    }

    private void doSerialize(ChunkSnapshot a, LootSerializationManager manager){
            int chunkX = a.getX();
            int chunkZ = a.getZ();


            List<SpawnArea> internal = createSpawnAreas(doFindSpawnForChunk(a));

            ChunkSpawnAreaHolder holder = new ChunkSpawnAreaHolder(chunkX, chunkZ, internal);
            manager.serializeChunkAreas(holder);
    }

    private List<SpawnArea> createSpawnAreas(ChunkRaritySnapshot snapshot) {
        List<SpawnArea> spawnAreas = new ArrayList<>(25);

        Stack<Point> toVisit = new Stack<>();
        List<Point> visited = new ArrayList<>();

        List<SpawnArea> spawnStream = generateInitialSpawnAreas(snapshot)
                .sorted((s1, s2) -> (-s1.getRarity() + s2.getRarity())).collect(Collectors.toList());

        int c = 0;

        for (SpawnArea spawnArea : spawnStream) {
            List<Point> localVisited = new ArrayList<>();
            toVisit.push(spawnArea.getMainPoint());

            if (visited.contains(spawnArea.getMainPoint()))
                continue;

            int rarity = spawnArea.getRarity();

            int xinit = spawnArea.getMainPoint().x;
            int zinit = spawnArea.getMainPoint().z;
            int yinit = spawnArea.getMainPoint().y;

            if (yinit == 0)
                continue;

            spawnArea = new SpawnArea(world, spawnArea.getRarity());

            while (!toVisit.empty()) {
                Point cur = toVisit.pop();

                // We need to do this to get chunk local rarity
                int x = cur.x - snapshot.getX() * 16;
                int y = cur.y;
                int z = cur.z - snapshot.getZ() * 16;

                if (visited.contains(cur) || localVisited.contains(cur))
                    continue;

                localVisited.add(cur);

                double dist = Math.sqrt((cur.x - xinit) * (cur.x - xinit) + (cur.y - yinit) * (cur.y - yinit) + (cur.z - zinit) * (cur.z - zinit));

                rarity = Math.max(rarity, snapshot.getRarity(x, y, z));

                // && rarity - 1 < snapshot.getRarity(x, y, z)
                //&& snapshot.getRarity(x, y, z) > 0 //  && initMat == snapshot.getSnapshot().getBlockType(x, y - 1, z)
                if (dist < 3 && snapshot.getRarity(x, y, z) > 0 && rarity - 2 < snapshot.getRarity(x, y, z)) {
                    spawnArea.addPoint(cur);

                    toVisit.push(new Point(cur.x + 1, cur.y, cur.z));
                    toVisit.push(new Point(cur.x - 1, cur.y, cur.z));
                    toVisit.push(new Point(cur.x, cur.y, cur.z + 1));
                    toVisit.push(new Point(cur.x, cur.y, cur.z - 1));
                    toVisit.push(new Point(cur.x + 1, cur.y + 1, cur.z));
                    toVisit.push(new Point(cur.x + 1, cur.y - 1, cur.z));
                    toVisit.push(new Point(cur.x - 1, cur.y + 1, cur.z));
                    toVisit.push(new Point(cur.x - 1, cur.y - 1, cur.z));
                    toVisit.push(new Point(cur.x, cur.y + 1, cur.z + 1));
                    toVisit.push(new Point(cur.x, cur.y - 1, cur.z + 1));
                    toVisit.push(new Point(cur.x, cur.y + 1, cur.z - 1));
                    toVisit.push(new Point(cur.x, cur.y - 1, cur.z - 1));
                }
            }

            if (spawnArea.getSize() > 3) {
                spawnArea.setRarity(rarity);
                spawnAreas.add(spawnArea);

                visited.addAll(localVisited);
            }

//            c = (c + 1) % 7;
//            c = (c + 1) % 15;
        }

        return spawnAreas;
    }


    private Stream<SpawnArea> generateInitialSpawnAreas(ChunkRaritySnapshot snapshot) {
        Stream.Builder<SpawnArea> builder = Stream.builder();

        int chunkX = snapshot.getX() * 16;
        int chunkZ = snapshot.getZ() * 16;
        for (int section = 0; section < 16; section++) {
            if (snapshot.getSnapshot().isSectionEmpty(section))
                continue;

            for (int x = 0; x < 16; x++) {
                for (int y = section * 16; y < section * 16 + 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        int belowRarity = snapshot.getRarity(x, y - 1, z);
                        int aboveRarity = snapshot.getRarity(x, y, z);
                        if (belowRarity == 0 && aboveRarity > 0) {
                            SpawnArea spawnArea = new SpawnArea(world, aboveRarity);
                            spawnArea.addPoint(new Point(x + chunkX, y, z + chunkZ));
                            builder.accept(spawnArea);
                        }
                    }
                }
            }
        }

        return builder.build();
    }


    private Stream<ChunkSnapshot> doFilterChunks(Stream<ChunkSnapshot> chunks) {
        return chunks
                .filter(Objects::nonNull)
                .filter(new ValidChunkFilter())
                .filter(new EnoughRoomFilter());
    }


    private ChunkRaritySnapshot doFindSpawnForChunk(ChunkSnapshot snapshot) {
        return new ChunkRaritySnapshot(snapshot, snapshot.getX(), snapshot.getZ(), this::getRarity);
    }

    private int getRarity(ChunkSnapshot snapshot, int x, int y, int z) {
        double rarity = 15;

        for (RarityModifier modifier : modifiers) {
            rarity = modifier.modify(x, y, z, snapshot, rarity);
        }

        return (int) rarity;
    }
}

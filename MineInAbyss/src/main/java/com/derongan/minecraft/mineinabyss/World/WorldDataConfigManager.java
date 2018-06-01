package com.derongan.minecraft.mineinabyss.World;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Configuration.ConfigurationConstants;
import com.derongan.minecraft.mineinabyss.MineInAbyss;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Functions;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class WorldDataConfigManager {
    private AbyssContext context;
    private static final String ENTITIES_KEY = "entities";

    public WorldDataConfigManager(AbyssContext context) {
        this.context = context;
    }

    public Map<Point, ChunkEntity> loadChunkData(Chunk chunk) {
        Path path = getChunkDataPath(chunk);

        if (path.toFile().exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

            return ((Collection<ChunkEntity>) config.getList(ENTITIES_KEY)).stream().collect(
                    Collectors.toMap(
                            c -> new Point(c.getX(), c.getY(), c.getZ()),
                            Functions.identity()
                    )
            );
        } else {
            return new HashMap<>();
        }
    }

    public void saveChunkData(Chunk chunk, Map<Point, ChunkEntity> entities) throws IOException {
        Path path = getChunkDataPath(chunk);

        // Recreate directories if missing
        path.toFile().getParentFile().mkdirs();

        YamlConfiguration config = new YamlConfiguration();

        //Values is not a normal collection that snake yaml knows how to
        //serialize, so we convert it to a raw array
        config.set(ENTITIES_KEY, entities.values().toArray());

        config.save(path.toFile());
    }

    @VisibleForTesting
    Path getChunkDataPath(Chunk chunk) {
        String worldDir = chunk.getWorld().getName();
        int x = chunk.getX();
        int z = chunk.getZ();

        return MineInAbyss.getInstance().getDataFolder()
                .toPath()
                .resolve(ConfigurationConstants.WORLD_ENTITY_DATA_DIR)
                .resolve(worldDir)
                .resolve(String.format("%d_%d.yml", x, z));
    }
}

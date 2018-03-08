package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Serialization;

import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.SpawnArea;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;


public class LootSerializationManager {
    private String outDir;
    private AbyssContext context;

    public LootSerializationManager(String outDir, AbyssContext context) {
        this.outDir = outDir;
        this.context = context;
    }

    public void serializeChunkAreas(ChunkSpawnAreaHolder holder) {
        if (holder.getSpawnAreas().size() == 0)
            return;

        Path serialPath = chunkToPath(holder.getChunkX(), holder.getChunkZ());

        try {
            if (!serialPath.resolve("..").toFile().exists()) {
                serialPath.resolve("..").toFile().mkdirs();
            }
            if (!serialPath.toFile().exists()) {
                serialPath.toFile().createNewFile();
            }

            String yamlString = serializeChunkAreasToString(holder);

            Files.write(serialPath, yamlString.getBytes());
        } catch (IOException e) {
            context.getLogger().warning("Failed to persist spawn areas");
        }
    }

    String serializeChunkAreasToString(ChunkSpawnAreaHolder holder) throws IOException {
        YamlConfiguration config = new YamlConfiguration();
        config.set("spawnareas", holder.getSpawnAreas());
        return config.saveToString();
    }

    public List<SpawnArea> deserializeChunk(Reader yamlReader) {
        List<SpawnArea> spawnAreas = (List<SpawnArea>) YamlConfiguration.loadConfiguration(yamlReader).get("spawnareas");

        if(spawnAreas == null)
            return spawnAreas;
        spawnAreas.forEach(a->a.updateWorld(context));

        return spawnAreas;
    }

    public Path chunkToPath(int chunkX, int chunkZ) {
        return Paths.get(outDir).resolve(String.format("%d_%d.yml", chunkX, chunkZ));
    }
}

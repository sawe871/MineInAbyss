package com.derongan.minecraft.mineinabyss.plugin;

import com.derongan.minecraft.mineinabyss.plugin.Ascension.AscensionData;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Layer;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Section;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.DistributionScanner;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Point;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Serialization.LootSerializationManager;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.SpawnArea;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;

/**
 * Stores context for the plugin, such as the plugin instance
 */
public class AbyssContext {
    private Map<UUID, AscensionData> playerAcensionDataMap = new HashMap<>();
    private Map<String, Layer> layerMap = new HashMap<>();
    private Plugin plugin;
    private Logger logger;
    private Configuration config;
    private int tickTime;

    private ConcurrentMap<String, ConcurrentMap<Point, ChunkSpawnAreaHolder>> worldRarity;

    public AbyssContext() {
        worldRarity = new ConcurrentHashMap<>(5);
    }

    public ChunkSpawnAreaHolder getSpawnAreas(String worldName, Point point) {
        ConcurrentMap<Point, ChunkSpawnAreaHolder> map = getOrCreateCacheForWorld(worldName);

        return map.computeIfAbsent(point, a -> {
            int chunkX = point.getX();
            int chunkZ = point.getZ();

            String filePath = plugin.getDataFolder().toPath().resolve("distribution").resolve(worldName).toString();

            LootSerializationManager someManager = new LootSerializationManager(filePath, AbyssContext.this);

            Path path = someManager.chunkToPath(chunkX, chunkZ);

            if (!path.toFile().exists()) {
                return new ChunkSpawnAreaHolder(chunkX, chunkZ, Collections.emptyList());
            }

            Reader reader;
            try {
                reader = new FileReader(path.toFile());
            } catch (FileNotFoundException e) {
                logger.warning("Failed to load chunk");
                return new ChunkSpawnAreaHolder(chunkX, chunkZ, Collections.emptyList());
            }

            return new ChunkSpawnAreaHolder(chunkX, chunkZ, someManager.deserializeChunk(reader));
        });
    }

    //TODO consider moving things into better places
    public ConcurrentMap<Point, ChunkSpawnAreaHolder> getOrCreateCacheForWorld(String worldName) {
        return worldRarity.computeIfAbsent(worldName, a -> new ConcurrentHashMap<>());
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public Map<UUID, AscensionData> getPlayerAcensionDataMap() {
        return playerAcensionDataMap;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public Map<String, Layer> getLayerMap() {
        return layerMap;
    }

    public int getTickTime() {
        return tickTime;
    }

    public void setTickTime(int tickTime) {
        this.tickTime = tickTime;
    }
}

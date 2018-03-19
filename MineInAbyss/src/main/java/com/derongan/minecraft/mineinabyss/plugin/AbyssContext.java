package com.derongan.minecraft.mineinabyss.plugin;

import com.derongan.minecraft.mineinabyss.plugin.Ascension.AscensionData;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Layer;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk.Point;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Serialization.LootSerializationManager;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

package com.derongan.minecraft.mineinabyss;

import com.derongan.minecraft.mineinabyss.Player.PlayerData;
import com.derongan.minecraft.mineinabyss.World.AbyssWorldManager;
import com.derongan.minecraft.mineinabyss.World.AbyssWorldManagerImpl;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk.Point;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Serialization.LootSerializationManager;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
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
    private Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private Plugin plugin;
    private Logger logger;
    private Configuration config;
    private int tickTime;

    private AbyssWorldManager worldManager;

    public AbyssContext(Configuration config) {
        this.config = config;
        worldManager = new AbyssWorldManagerImpl(getConfig());
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
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

    public AbyssWorldManager getWorldManager() {
        return worldManager;
    }
}

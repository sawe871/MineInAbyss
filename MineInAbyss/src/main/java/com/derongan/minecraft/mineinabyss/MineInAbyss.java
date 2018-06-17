package com.derongan.minecraft.mineinabyss;

import com.derongan.minecraft.mineinabyss.Ascension.AscensionCommandExecutor;
import com.derongan.minecraft.mineinabyss.Ascension.AscensionListener;
import com.derongan.minecraft.mineinabyss.Configuration.ConfigurationManager;
import com.derongan.minecraft.mineinabyss.Player.PlayerData;
import com.derongan.minecraft.mineinabyss.Player.PlayerDataConfigManager;
import com.derongan.minecraft.mineinabyss.Player.PlayerListener;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.DistributionCommandExecutor;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.DistributionTask;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.SpawnArea;
import com.derongan.minecraft.mineinabyss.Relic.Loading.RelicLoader;
import com.derongan.minecraft.mineinabyss.Relic.executors.RelicCommandExecutor;
import com.derongan.minecraft.mineinabyss.Relic.RelicDecayTask;
import com.derongan.minecraft.mineinabyss.Relic.RelicGroundEntity;
import com.derongan.minecraft.mineinabyss.Relic.RelicUseListener;
import com.derongan.minecraft.mineinabyss.World.EntityChunkListener;
import com.derongan.minecraft.mineinabyss.World.EntityChunkManager;
import com.derongan.minecraft.mineinabyss.World.Point;
import com.derongan.minecraft.mineinabyss.World.executors.WorldCommandExecutor;
import com.derongan.minecraft.mineinabyss.util.TickUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Arrays;

public final class MineInAbyss extends JavaPlugin {
    private final int TICKS_BETWEEN = 5;
    private AbyssContext context;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("On enable has been called");
        ConfigurationManager.createConfig(this);

        context = new AbyssContext(getConfig());
        context.setPlugin(this);
        context.setLogger(getLogger());

        getServer().getPluginManager().registerEvents(new PlayerListener(context), this);
        getServer().getPluginManager().registerEvents(new AscensionListener(context), this);
        getServer().getPluginManager().registerEvents(new EntityChunkListener(context), this);

        PlayerDataConfigManager manager = new PlayerDataConfigManager(context);

        getServer().getOnlinePlayers().forEach((player) ->
                context.getPlayerDataMap().put(
                        player.getUniqueId(),
                        manager.loadPlayerData(player))
        );

        Runnable decayTask = new RelicDecayTask(TICKS_BETWEEN);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, decayTask, TICKS_BETWEEN, TICKS_BETWEEN);
        getServer().getPluginManager().registerEvents(new RelicUseListener(context), this);
        RelicCommandExecutor relicCommandExecutor = new RelicCommandExecutor(context);
        this.getCommand("relic").setExecutor(relicCommandExecutor);
        this.getCommand("relicreload").setExecutor(relicCommandExecutor);
        this.getCommand("relics").setExecutor(relicCommandExecutor);
        this.getCommand("yolo").setExecutor(relicCommandExecutor);

        WorldCommandExecutor worldCommandExecutor = new WorldCommandExecutor(context);

        this.getCommand("sectionon").setExecutor(worldCommandExecutor);
        this.getCommand("sectionoff").setExecutor(worldCommandExecutor);

        AscensionCommandExecutor ascensionCommandExecutor = new AscensionCommandExecutor(context);

        this.getCommand("curseon").setExecutor(ascensionCommandExecutor);
        this.getCommand("curseoff").setExecutor(ascensionCommandExecutor);

        ConfigurationSerialization.registerClass(RelicGroundEntity.class);

        DistributionCommandExecutor distributionCommandExecutor = new DistributionCommandExecutor(context);

        this.getCommand("preparelootareas").setExecutor(distributionCommandExecutor);

        ConfigurationSerialization.registerClass(SpawnArea.class);
        ConfigurationSerialization.registerClass(Point.class);

        Runnable distributionTask = new DistributionTask(context, context.getWorldManager().getLayerAt(0));
//        getServer().getScheduler().scheduleSyncRepeatingTask(this, distributionTask, TickUtils.milisecondsToTicks(1000), TickUtils.milisecondsToTicks(1000));

        // Load all chunks
        getServer().getWorlds().forEach(a->{
            Arrays.stream(a.getLoadedChunks()).forEach(context.getEntityChunkManager()::loadChunk);
        });

        RelicLoader.loadAllRelics(context);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        PlayerDataConfigManager playerManager = new PlayerDataConfigManager(context);
        EntityChunkManager chunkManager = context.getEntityChunkManager();

        getServer().getOnlinePlayers().forEach(player -> {
            PlayerData data = context.getPlayerDataMap().get(player.getUniqueId());
            try {
                playerManager.savePlayerData(data);
            } catch (IOException e) {
                getLogger().warning("Error saving player data for "+player.getUniqueId());
                e.printStackTrace();
            }
        });


        // Clean up all chunks
        chunkManager.disable();

        getLogger().info("onDisable has been invoked!");
    }


    public static MineInAbyss getInstance() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("MineInAbyss");

        return (MineInAbyss) plugin;
    }

    public AbyssContext getContext() {
        return context;
    }
}

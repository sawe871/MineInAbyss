package com.derongan.minecraft.mineinabyss.plugin;

import com.derongan.minecraft.mineinabyss.plugin.Ascension.AscensionCommandExecutor;
import com.derongan.minecraft.mineinabyss.plugin.Ascension.AscensionListener;
import com.derongan.minecraft.mineinabyss.plugin.Ascension.AscensionTask;
import com.derongan.minecraft.mineinabyss.plugin.Configuration.ConfigurationManager;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Layer;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.*;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk.Point;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Loading.RelicLoader;
import com.derongan.minecraft.mineinabyss.plugin.Relic.RelicCommandExecutor;
import com.derongan.minecraft.mineinabyss.plugin.Relic.RelicDecayTask;
import com.derongan.minecraft.mineinabyss.plugin.Relic.RelicUseListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class MineInAbyss extends JavaPlugin {
    private final int TICKS_BETWEEN = 5;
    AbyssContext context;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("On enable has been called");
        ConfigurationManager.createConfig(this);

        ConfigurationSerialization.registerClass(SpawnArea.class);
        ConfigurationSerialization.registerClass(Point.class);

        context = new AbyssContext();
        context.setPlugin(this);
        context.setLogger(getLogger());
        context.setConfig(getConfig());
        context.setTickTime(TICKS_BETWEEN);

        Layer prev = null;
        for (Map layerData : getConfig().getMapList("layers")) {
            Layer layer = new Layer((String) layerData.get("name"), context);

            List<List<Integer>> sectionOffsets = (List<List<Integer>>) layerData.get("sectionOffsets");

            if (sectionOffsets != null) {
                context.getLogger().info("Section data found");

            } else {
                context.getLogger().info("No section data");
                sectionOffsets = new ArrayList<>();
            }

            List<List<Integer>> sectionAreas = (List<List<Integer>>) layerData.get("sectionAreas");

            if (sectionAreas != null) {
                context.getLogger().info("Section area data found");

            } else {
                context.getLogger().info("No section area data");
                sectionAreas = new ArrayList<>();
            }

            layer.setSectionsOnLayer(sectionOffsets, sectionAreas, getServer().getWorld(layer.getName()));
            layer.setEffectsOnLayer((Collection<Map>) layerData.get("effects"));
            layer.setDeathMessage((String) layerData.getOrDefault("abyssDeathMessage", null));
            layer.setOffset((int) layerData.getOrDefault("offset", 50));
            context.getLayerMap().put(layer.getName(), layer);

            layer.setPrevLayer(prev);
            if (prev != null) {
                prev.setNextLayer(layer);
            }
            prev = layer;
        }




        RelicLoader.loadAllRelics(context);
        setupCommandExecutors();
        setupTasks();
    }

    private void setupTasks(){
        Runnable mainTask = new AscensionTask(context, TICKS_BETWEEN);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, mainTask, TICKS_BETWEEN, TICKS_BETWEEN);

        Runnable decayTask = new RelicDecayTask(TICKS_BETWEEN);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, decayTask, TICKS_BETWEEN, TICKS_BETWEEN);

        getServer().getPluginManager().registerEvents(new AscensionListener(context), this);
        getServer().getPluginManager().registerEvents(new RelicUseListener(), this);

        getServer().getWorlds().forEach(a->{
            DistributionTask distributionTask = new DistributionTask(context, a);
            if(distributionTask.shouldSchedule())
                distributionTask.runTaskTimer(this, TICKS_BETWEEN, TickUtils.milisecondsToTicks(300));
        });
    }


    private void setupCommandExecutors() {
        RelicCommandExecutor relicCommandExecutor = new RelicCommandExecutor(context);
        this.getCommand("relic").setExecutor(relicCommandExecutor);
        this.getCommand("relicreload").setExecutor(relicCommandExecutor);
        this.getCommand("relics").setExecutor(relicCommandExecutor);

        AscensionCommandExecutor ascensionCommandExecutor = new AscensionCommandExecutor(context);
        this.getCommand("sectionon").setExecutor(ascensionCommandExecutor);
        this.getCommand("sectionoff").setExecutor(ascensionCommandExecutor);

        DistributionCommandExecutor distributionCommandExecutor = new DistributionCommandExecutor(context);
        this.getCommand("preparelootareas").setExecutor(distributionCommandExecutor);
        this.getCommand("loadlootareas").setExecutor(distributionCommandExecutor);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("onDisable has been invoked!");
    }
}

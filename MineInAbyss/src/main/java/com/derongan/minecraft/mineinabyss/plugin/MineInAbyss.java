package com.derongan.minecraft.mineinabyss.plugin;

import com.derongan.minecraft.mineinabyss.plugin.Ascension.AscensionCommandExecutor;
import com.derongan.minecraft.mineinabyss.plugin.Ascension.AscensionListener;
import com.derongan.minecraft.mineinabyss.plugin.Ascension.AscensionTask;
import com.derongan.minecraft.mineinabyss.plugin.Configuration.ConfigurationManager;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Layer;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Loading.RelicLoader;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Looting.DistributionScanner;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Looting.DistributionTask;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Looting.LootableRelicScanner;
import com.derongan.minecraft.mineinabyss.plugin.Relic.RelicCommandExecutor;
import com.derongan.minecraft.mineinabyss.plugin.Relic.RelicDecayTask;
import com.derongan.minecraft.mineinabyss.plugin.Relic.RelicUseListener;
import org.bukkit.ChunkSnapshot;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
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

        context = new AbyssContext();
        context.setPlugin(this);
        context.setLogger(getLogger());
        context.setConfig(getConfig());
        context.setTickTime(TICKS_BETWEEN);

        Layer prev = null;
        for (Map layerData : getConfig().getMapList("layers")) {
            Layer layer = new Layer((String) layerData.get("name"), context);

            List<List<Integer>> sections = (List<List<Integer>>) layerData.get("sectionOffsets");

            if (sections != null) {
                context.getLogger().info("Section data found");

            } else {
                context.getLogger().info("No section data");
                sections = new ArrayList<>();
            }

            layer.setSectionsOnLayer(sections, getServer().getWorld(layer.getName()));
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

        Runnable mainTask = new AscensionTask(context, TICKS_BETWEEN);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, mainTask, TICKS_BETWEEN, TICKS_BETWEEN);

        Runnable decayTask = new RelicDecayTask(TICKS_BETWEEN);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, decayTask, TICKS_BETWEEN, TICKS_BETWEEN);

//        LootableRelicScanner scanner = new LootableRelicScanner();
//        scanner.clearAllRelics(getServer().getWorlds());

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (getServer().getWorld("LayerOne") != null) {
                DistributionScanner distributionScanner = new DistributionScanner(getServer().getWorld("LayerOne"), context);
                List<ChunkSnapshot> validChunks = distributionScanner.scan(new Point(190, 0), new Point(124, 60));

                Runnable lootTask = new DistributionTask(context, getServer().getWorld("LayerOne"), validChunks);
                getServer().getScheduler().scheduleSyncRepeatingTask(this, lootTask, TICKS_BETWEEN, 1);
            } else {
                getLogger().info("Bad world");
            }
        }, TickUtils.milisecondsToTicks(1000));

        getServer().getPluginManager().registerEvents(new AscensionListener(context), this);
        getServer().getPluginManager().registerEvents(new RelicUseListener(), this);


        RelicCommandExecutor relicCommandExecutor = new RelicCommandExecutor(context);
        this.getCommand("relic").setExecutor(relicCommandExecutor);
        this.getCommand("relicreload").setExecutor(relicCommandExecutor);
        this.getCommand("relics").setExecutor(relicCommandExecutor);

        AscensionCommandExecutor ascensionCommandExecutor = new AscensionCommandExecutor(context);
        this.getCommand("sectionon").setExecutor(ascensionCommandExecutor);
        this.getCommand("sectionoff").setExecutor(ascensionCommandExecutor);

        RelicLoader.loadAllRelics(context);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("onDisable has been invoked!");
    }
}

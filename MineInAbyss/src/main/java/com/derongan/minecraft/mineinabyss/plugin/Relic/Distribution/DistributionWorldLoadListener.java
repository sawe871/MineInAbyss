package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Layer;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Section;
import com.google.common.cache.LoadingCache;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class DistributionWorldLoadListener implements Listener {
    private AbyssContext context;

    public DistributionWorldLoadListener(AbyssContext context) {
        this.context = context;
    }

    @EventHandler()
    public void onWorldLoad(WorldLoadEvent worldLoadEvent) {
        World world = worldLoadEvent.getWorld();
        context.getOrCreateCacheForWorld(world.getName());
    }
}

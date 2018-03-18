package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.TickUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DistributionWorldLoadListener implements Listener {
    private AbyssContext context;

    public DistributionWorldLoadListener(AbyssContext context) {
        this.context = context;
    }

    @EventHandler()
    public void onWorldLoad(WorldLoadEvent worldLoadEvent) {
        World world = worldLoadEvent.getWorld();

        DistributionTask distributionTask = new DistributionTask(context, world);

        if(distributionTask.shouldSchedule())
            distributionTask.runTaskTimer(context.getPlugin(), 0, TickUtils.milisecondsToTicks(300));
    }
}

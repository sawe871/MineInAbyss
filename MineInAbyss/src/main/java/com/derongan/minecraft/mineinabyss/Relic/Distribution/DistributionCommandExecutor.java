package com.derongan.minecraft.mineinabyss.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Scanning.DistributionScanner;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Serialization.LootSerializationManager;
import com.derongan.minecraft.mineinabyss.World.Point;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.World.AbyssWorldManager;
import com.derongan.minecraft.mineinabyss.World.Section;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class DistributionCommandExecutor implements CommandExecutor {
    private AbyssContext context;

    public DistributionCommandExecutor(AbyssContext context) {
        this.context = context;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!commandSender.isOp()) {
            commandSender.sendMessage("In your dreams.");
            return true;
        }
        if (args.length < 1)
            return false;

        String layerIndex = args[0];

        if (label.equals("preparelootareas") && StringUtils.isNumeric(layerIndex)) {
            prepareLootAreas(Integer.valueOf(layerIndex));

            return true;
        }
        return false;
    }

    private void prepareLootAreas(int layerIndex) {
        AbyssWorldManager manager = context.getWorldManager();

        manager.getLayerAt(layerIndex).getSections().forEach(this::prepareLootAreas);
    }

    private void prepareLootAreas(Section section) {
        if (section.getArea() == null)
            return;

        Point top = section.getArea().getFirstCorner();
        Point bottom = section.getArea().getSecondCorner();

        final String outDir = String.format("section_%d", section.getIndex());
        final Path path = context.getPlugin().getDataFolder().toPath().resolve("distribution").resolve(outDir);
        try {
            path.toFile().mkdirs();
            FileUtils.cleanDirectory(path.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.broadcastMessage(String.format("Starting generating %s section %d. Bye", section.getLayer().getName(), section.getIndex()));

        DistributionScanner scanner = new DistributionScanner(section.getWorld(), context);


        Iterator<List<ChunkSnapshot>> iterator = scanner.scan(top, bottom, path, section.getIndex());

        LootSerializationManager manager = new LootSerializationManager(path.normalize().toString(), context);
    }
}

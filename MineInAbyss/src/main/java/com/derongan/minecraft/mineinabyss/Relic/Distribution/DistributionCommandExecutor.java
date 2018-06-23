package com.derongan.minecraft.mineinabyss.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.Player.PlayerData;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Scanning.DistributionScannerImpl;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Serialization.LootSerializationManager;
import com.derongan.minecraft.mineinabyss.World.Layer;
import com.derongan.minecraft.mineinabyss.World.Point;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.World.AbyssWorldManager;
import com.derongan.minecraft.mineinabyss.World.Section;
import com.google.common.collect.Iterators;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


        if (label.equals("preparelootareas")) {

            if (args.length < 1)
                return false;

            String layerIndex = args[0];

            if (StringUtils.isNumeric(layerIndex)) {
                prepareLootAreas(Integer.valueOf(layerIndex));
                return true;
            }
        }

        if (label.equals("showloot")) {
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                context.getPlayerDataMap().get(player.getUniqueId()).setCanSeeLootSpawns(true);
                player.sendMessage("Loot spawns now shown");
                return true;
            }
        }

        if (label.equals("hideloot")) {
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                context.getPlayerDataMap().get(player.getUniqueId()).setCanSeeLootSpawns(false);
                player.sendMessage("Loot spawns now hidden");
                return true;
            }
        }

        return false;
    }

    private void prepareLootAreas(int layerIndex) {
        AbyssWorldManager manager = context.getWorldManager();

        Layer layer = manager.getLayerAt(layerIndex);

        if (layer == null) {
            Bukkit.broadcastMessage(String.format("There is no layer %d", layerIndex));
            return;
        }

        layer.getSections().forEach(this::prepareLootAreas);
    }

    private void prepareLootAreas(Section section) {
        if (section.getArea() == null) {
            Bukkit.broadcastMessage(String.format("No area defined for %s section %d. Bye", section.getLayer().getName(), section.getIndex()));
            return;
        }


        final String outDir = String.format("section_%d", section.getIndex());
        final Path path = context.getPlugin().getDataFolder().toPath().resolve("distribution").resolve(outDir);
        try {
            path.toFile().mkdirs();
            FileUtils.cleanDirectory(path.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.broadcastMessage(String.format("Starting generating %s section %d. Lag will occur.", section.getLayer().getName(), section.getIndex()));

        DistributionScannerImpl scanner = new DistributionScannerImpl(section.getWorld(), context);


        Stream<ChunkSpawnAreaHolder> spawnAreaHolderStream = scanner.scan(section);

        LootSerializationManager manager = new LootSerializationManager(path.normalize().toString(), context);

        Iterator<List<ChunkSpawnAreaHolder>> iterator = Iterators.partition(spawnAreaHolderStream.iterator(), 100);

        doSaveAreas(iterator, manager, section);
    }

    private void doSaveAreas(Iterator<List<ChunkSpawnAreaHolder>> holders, LootSerializationManager manager, Section section) {
        if (holders.hasNext()) {
            holders.next().forEach(manager::serializeChunkAreas);
            Bukkit.getScheduler().scheduleSyncDelayedTask(context.getPlugin(), () -> {
                doSaveAreas(holders, manager, section);
            }, 100);
        } else {
            Bukkit.broadcastMessage(String.format("Finished generating %s section %d. Bye", section.getLayer().getName(), section.getIndex()));
        }
    }
}

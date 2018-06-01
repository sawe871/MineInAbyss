package com.derongan.minecraft.mineinabyss.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.Relic.Distribution.Scanning.DistributionScanner;
import com.derongan.minecraft.mineinabyss.World.Point;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.World.AbyssWorldManager;
import com.derongan.minecraft.mineinabyss.World.Section;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Path;

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

        String worldName = args[0];

        if (label.equals("preparelootareas")) {
            return prepareLootAreas(worldName);
        }
        return false;
    }

    private boolean prepareLootAreas(String worldName) {
        World world = context.getPlugin().getServer().getWorld(worldName);

        AbyssWorldManager manager = context.getWorldManager();

        Section tsec = manager.getSectonAt(0);

        Point top = tsec.getArea().getFirstCorner();
        Point bottom = tsec.getArea().getSecondCorner();

        final String outDir = String.format("section_%d", 1);
        final Path path = context.getPlugin().getDataFolder().toPath().resolve("distribution").resolve(outDir);
        try {
            path.toFile().mkdirs();
            FileUtils.cleanDirectory(path.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.broadcastMessage(String.format("Starting generating %s section %d. Bye", worldName, 1));

        DistributionScanner scanner = new DistributionScanner(world, context);
        scanner.scan(top, bottom, path, 1);

        return true;
    }
}

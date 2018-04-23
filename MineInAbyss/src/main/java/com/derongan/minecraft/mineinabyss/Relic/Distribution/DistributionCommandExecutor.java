package com.derongan.minecraft.mineinabyss.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.Point;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DistributionCommandExecutor implements CommandExecutor {
    private AbyssContext context;

    public DistributionCommandExecutor(AbyssContext context) {
        this.context = context;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(!commandSender.isOp()){
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

        if (world == null)
            return false;

        // TODO dont iterate
        List<Map> layers = (List<Map>) context.getConfig().get("layers");
        Map layer = layers.stream().filter(a -> a.get("name").equals(worldName)).findAny().get();

        if (layer == null) {
            return false;
        }

        List<List<Integer>> sectionAreas = (List<List<Integer>>) layer.get("sectionAreas");

        for (int i = 0; i < sectionAreas.size(); i++) {
            List<Integer> area = sectionAreas.get(i);
            Point top = new Point(area.get(0), 0, area.get(1));
            Point bottom = new Point(area.get(2), 0, area.get(3));

            final String outDir = String.format(world.getName() + "/section_%d", i);
            final Path path = context.getPlugin().getDataFolder().toPath().resolve("distribution").resolve(outDir);
            try {
                path.toFile().mkdirs();
                FileUtils.cleanDirectory(path.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }


            Bukkit.broadcastMessage(String.format("Starting generating %s section %d. Bye", worldName, i));


            DistributionScanner scanner = new DistributionScanner(world, context);
            scanner.scan(top, bottom, path, i);
        }
        return true;
    }
}

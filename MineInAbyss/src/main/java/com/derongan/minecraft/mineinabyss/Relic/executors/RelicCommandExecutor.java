package com.derongan.minecraft.mineinabyss.Relic.executors;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Loading.RelicLoader;
import com.derongan.minecraft.mineinabyss.Relic.RelicGroundEntity;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.Relic.Relics.StandardRelicType;
import com.derongan.minecraft.mineinabyss.World.ChunkEntity;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RelicCommandExecutor implements CommandExecutor {
    private AbyssContext context;

    public RelicCommandExecutor(AbyssContext context) {
        this.context = context;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (label.equals("relic")) {
                if (args.length == 0) {
                    return false;
                }
                for (RelicType relicType : RelicType.registeredRelics.values()) {
                    if (relicType.getName().replace(" ", "_").toLowerCase().equals(args[0].toLowerCase())) {
                        player.getInventory().addItem(relicType.getItem());
                        return true;
                    }
                }
            }

            if (label.equals("relicreload")) {
                RelicLoader.unloadAllRelics();
                RelicLoader.loadAllRelics(context);
                return true;
            }

            if(label.equals("relics")){
                player.sendMessage("Relics: " + RelicType.registeredRelics.values().stream()
                        .map(RelicType::getName)
                        .map(a->a.replace(  " ", "_"))
                        .collect(Collectors.joining(", ")));
                return true;
            }

            if(label.equals("testspawn") && player.isOp()){
                Location location = player.getLocation();

                int seconds = 10;
                if(args.length != 0 && StringUtils.isNumeric(args[0]))
                    seconds = Double.valueOf(args[0]).intValue();

                RelicType blaze = StandardRelicType.BLAZE_REAP;
                ChunkEntity entity = new RelicGroundEntity(blaze,
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        TimeUnit.SECONDS.toMillis(seconds));
                context.getEntityChunkManager().addEntity(location.getChunk(), entity);
            }
        }

        return false;
    }
}

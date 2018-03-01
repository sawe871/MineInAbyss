package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution;

import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Scan the map for existing relics
 */
public class LootableRelicScanner {
    /**
     * Return a list of (likely) placed relic entities. Note that this is naive
     * and will include any invisible invulnerable armorstand.
     * @param world The world to do the scan on
     * @return A list of entities that are (probably) lootable relics
     */
    public List<Entity> getLootableRelics(World world) {
        return world.getEntitiesByClass(ArmorStand.class)
                .stream()
                .filter(a -> !a.isVisible() && a.isInvulnerable()).collect(Collectors.toList());
    }

    public void removeLootableRelics(List<Entity> entities){
        entities.forEach(Entity::remove);
    }
    public void clearAllRelics(List<World> worlds){
        for(World world: worlds){
            List<Entity> relicEntities = this.getLootableRelics(world);
            this.removeLootableRelics(relicEntities);
        }
    }
}

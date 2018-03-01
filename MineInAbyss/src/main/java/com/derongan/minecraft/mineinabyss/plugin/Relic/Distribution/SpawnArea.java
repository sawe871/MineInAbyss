package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;

import java.util.*;

@SerializableAs("spawnarea")
public class SpawnArea implements ConfigurationSerializable{
    private List<Point> blocks;
    private World world;
    private String worldName;

    private int rarity;

    private SpawnArea() {
    }

    private SpawnArea(World world, int rarity, List<Point> blocks) {
        this.blocks = blocks;
        this.rarity = rarity;
        this.world = world;

        this.worldName = world.getName();
    }

    public SpawnArea(List<Point> blocks, String worldName, int rarity) {
        this.blocks = blocks;
        this.worldName = worldName;
        this.rarity = rarity;
    }

    public SpawnArea(World world, int rarity) {
        this(world, rarity, new ArrayList<>());
    }

    public void updateWorld(AbyssContext context){
        this.world = context.getPlugin().getServer().getWorld(worldName);
    }

    public int getSize() {
        return blocks.size();
    }

    public void addPoint(Point point) {
        blocks.add(point);
    }

    public void displayRegion() {
        blocks.forEach(
                a -> {
                    Vector l = new Vector(a.x + .5, a.y + .5, a.z + .5);
                    double x = l.getX();
                    double y = l.getY();
                    double z = l.getZ();
                    world.spawnParticle(Particle.REDSTONE, x, y, z, 0, Math.min(rarity / 15.0 + .000001, 1), Math.max(1 - rarity / 15.0 + .000001, 0), 0, 1);
//                    spawnIt(x,y,z, rarity);
                }
        );
    }

    public void spawnIt(double x, double y, double z, int rarity) {
        if (rarity == 0)
            world.spawnParticle(Particle.REDSTONE, x, y, z, 0, 1, .00001, .00001, 1);
        if (rarity == 1)
            world.spawnParticle(Particle.REDSTONE, x, y, z, 0, .00001, 1, .00001, 1);
        if (rarity == 2)
            world.spawnParticle(Particle.REDSTONE, x, y, z, 0, .00001, .00001, 1, 1);
        if (rarity == 3)
            world.spawnParticle(Particle.REDSTONE, x, y, z, 0, 1, 1, .00001, 1);
        if (rarity == 4)
            world.spawnParticle(Particle.REDSTONE, x, y, z, 0, 1, .00001, 1, 1);
        if (rarity == 5)
            world.spawnParticle(Particle.REDSTONE, x, y, z, 0, .00001, 1, 1, 1);
        if (rarity == 6)
            world.spawnParticle(Particle.REDSTONE, x, y, z, 0, 1, 1, 1, 1);
    }


    public Point getMainPoint() {
        return blocks.get(0);
    }


    /*
    Below is mostly used for serialization
     */

    public List<Point> getBlocks() {
        return blocks;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getRarity() {
        return rarity;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();

        serialized.put("worldName", worldName);
        serialized.put("rarity", rarity);
        serialized.put("blocks", blocks);

        return serialized;
    }

    public static SpawnArea deserialize(Map<String,Object> data){
        String worldName = (String) data.get("worldName");
        int rarity = (int) data.get("rarity");

        List<Point> blocks = (List<Point>) data.get("blocks");

        return new SpawnArea(blocks, worldName, rarity);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof SpawnArea))
            return false;

        SpawnArea other = (SpawnArea) o;

        return other.worldName.equals(worldName) && other.blocks.equals(this.blocks) && other.rarity == rarity;
    }

    public Point getRandomPoint(){
        return blocks.get(new Random().nextInt(blocks.size()));
    }
}

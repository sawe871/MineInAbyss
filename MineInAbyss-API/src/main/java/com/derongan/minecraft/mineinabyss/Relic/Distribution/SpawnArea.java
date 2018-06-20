package com.derongan.minecraft.mineinabyss.Relic.Distribution;

import com.derongan.minecraft.mineinabyss.World.Point;
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

    public void setWorld(World world){
        this.world = world;
    }

    public int getSize() {
        return blocks.size();
    }

    public void addPoint(Point point) {
        blocks.add(point);
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

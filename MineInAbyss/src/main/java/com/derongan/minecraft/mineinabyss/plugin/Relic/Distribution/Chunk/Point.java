package com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;


//TODO what package should this be under?
@SerializableAs("point")
public class Point implements ConfigurationSerializable {
    public int x, y, z;

    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point))
            return false;
        Point other = (Point) o;
        return other.x == x && other.y == y && other.z == z;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("x",x);
        serialized.put("y",y);
        serialized.put("z",z);

        return serialized;
    }

    public static Point deserialize(Map<String, Object> data){
        int x = (int) data.get("x");
        int y = (int) data.get("y");
        int z = (int) data.get("z");

        return new Point(x,y,z);
    }

    @Override
    public String toString() {
        return String.format("Point: %d %d %d", x, y, z);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}

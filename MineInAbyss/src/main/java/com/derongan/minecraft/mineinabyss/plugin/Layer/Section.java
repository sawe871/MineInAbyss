package com.derongan.minecraft.mineinabyss.plugin.Layer;

import com.derongan.minecraft.mineinabyss.plugin.Relic.Distribution.Chunk.Point;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Section {
    private final Vector offset;
    private final int sharedWithBelow;
    private final World world;

    private Point top;
    private Point bottom;

    private boolean hasRegions = false;


    public Section(Vector offset, int sharedWithBelow, World world) {
        this.offset = offset;
        this.sharedWithBelow = sharedWithBelow;
        this.world = world;
    }

    public void setupRegion(Point top, Point bottom){
        this.top = top;
        this.bottom = bottom;

        hasRegions = true;
    }

    public Vector getOffset() {
        return offset;
    }

    public int getSharedWithBelow() {
        return sharedWithBelow;
    }

    public World getWorld() {
        return world;
    }

    public Point getTop() {
        return top;
    }

    public Point getBottom() {
        return bottom;
    }

    public boolean hasRegions() {
        return hasRegions;
    }
}

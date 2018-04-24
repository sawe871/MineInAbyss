package com.derongan.minecraft.mineinabyss.World;


import org.bukkit.Location;
import org.bukkit.World;

public class SectionImpl implements Section {
    private int index;

    private World world;
    private Layer layer;

    private Location referenceTop;
    private Location referenceBottom;

    private SectionArea area;

    public SectionImpl(int index, World world, Layer layer, Location referenceTop, Location referenceBottom) {
        this.index = index;
        this.world = world;
        this.layer = layer;
        this.referenceTop = referenceTop;
        this.referenceBottom = referenceBottom;
    }

    @Override
    public Location getReferenceLocationTop() {
        return referenceTop;
    }

    @Override
    public Location getReferenceLocationBottom() {
        return referenceBottom;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public Layer getLayer() {
        return layer;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public String getWorldName() {
        return world.getName();
    }

    @Override
    public SectionArea getArea() {
        return area;
    }

    public void setArea(int x1, int y1, int x2, int y2) {
        this.area = new SectionAreaImpl(x1, y1, x2, y2);
    }
}

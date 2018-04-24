package com.derongan.minecraft.mineinabyss.World;

import org.bukkit.util.Vector;

public class SectionAreaImpl implements SectionArea{
    private Point firstCorner;
    private Point secondCorner;

    public SectionAreaImpl(int x1, int z1, int x2, int z2) {
        firstCorner = new Point(x1,0,z1);
        secondCorner = new Point(x2,0,z2);
    }

    @Override
    public Point getFirstCorner() {
        return firstCorner;
    }

    @Override
    public Point getSecondCorner() {
        return secondCorner;
    }
}

package com.derongan.minecraft.mineinabyss.util;

public class TickUtils {
    public static long milisecondsToTicks(long mili){
        return mili / 50L;
    }

    public static int milisecondsToTicks(int mili){
        return mili / 50;
    }
    public static int ticksToMilliseconds(int ticks){
        return ticks * 50;
    }
}

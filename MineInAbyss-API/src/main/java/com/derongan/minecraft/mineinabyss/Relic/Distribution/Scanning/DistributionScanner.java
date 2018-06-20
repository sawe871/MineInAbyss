package com.derongan.minecraft.mineinabyss.Relic.Distribution.Scanning;

import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.SpawnArea;
import com.derongan.minecraft.mineinabyss.World.Section;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Interface to be implemented for scanners that scan the world
 * to generate spawn areas for items.
 */
public interface DistributionScanner {
    /**
     * Scan an area and generate spawn areas.
     * @param section The section to scan
     * @return A stream of SpawnAreas for that section.
     */
    Stream<ChunkSpawnAreaHolder> scan(Section section);
}

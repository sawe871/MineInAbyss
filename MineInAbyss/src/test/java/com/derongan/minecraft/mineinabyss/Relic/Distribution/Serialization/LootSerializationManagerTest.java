package com.derongan.minecraft.mineinabyss.Relic.Distribution.Serialization;


import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.ChunkSpawnAreaHolder;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.Chunk.Point;
import com.derongan.minecraft.mineinabyss.Relic.Distribution.SpawnArea;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


class LootSerializationManagerTest {
    private LootSerializationManager serializer;
    private SpawnArea area;

    private static final String chunkYaml = "spawnareas:\n" +
            "- ==: spawnarea\n" +
            "  worldName: Somewhere\n" +
            "  blocks:\n" +
            "  - ==: point\n" +
            "    x: 1\n" +
            "    y: 1\n" +
            "    z: 1\n" +
            "  rarity: 5\n";

    @BeforeEach
    void setup() throws IOException {
        AbyssContext mockContext = mock(AbyssContext.class);
        serializer = new LootSerializationManager("", mockContext);

        Point point = new Point(1, 1, 1);

        World mockWorld = mock(World.class);
        Plugin mockPlugin = mock(Plugin.class);
        Server mockServer = mock(Server.class);

        doReturn("Somewhere").when(mockWorld).getName();
        doReturn(mockWorld).when(mockServer).getWorld(anyString());
        doReturn(mockServer).when(mockPlugin).getServer();
        doReturn(mockPlugin).when(mockContext).getPlugin();

        area = new SpawnArea(mockWorld, 5);
        area.addPoint(point);
    }

    @Test
    void serializeChunkAreasStringAsExpected() throws IOException {
        ChunkSpawnAreaHolder holder = new ChunkSpawnAreaHolder(0, 0, Arrays.asList(area));
        String output = serializer.serializeChunkAreasToString(holder);

        assertEquals(chunkYaml, output);
    }

    @Test
    void deserialzeChunkAreasAsExpected() throws IOException {
        ConfigurationSerialization.registerClass(SpawnArea.class);
        ConfigurationSerialization.registerClass(Point.class);

        Reader reader = new StringReader(chunkYaml);

        List<SpawnArea> spawnAreas = serializer.deserializeChunk(reader);

        assertEquals(Arrays.asList(area), spawnAreas);
    }

}
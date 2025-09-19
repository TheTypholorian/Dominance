package net.typho.dominance.client;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.LinkedHashMap;
import java.util.Map;

public record DimensionLightInfo(float[] minBlock, float[] maxBlock, float[] minTemp, float[] maxTemp, float[] minHumid, float[] maxHumid, float[] blockScale, float[] skyScale, boolean hasDay) {
    public static final Map<Identifier, DimensionLightInfo> MAP = new LinkedHashMap<>();
    public static final DimensionLightInfo OVERWORLD = new DimensionLightInfo(
            new float[]{1f, 0.85f, 0.65f}, new float[]{1f, 0.95f, 0.9f},
            new float[]{0.75f, 0.9f, 1f}, new float[]{1f, 1f, 0.9f},
            null, null,
            null, null,
            true
    );
    public static final DimensionLightInfo NETHER = new DimensionLightInfo(
            new float[]{1f, 0.65f, 0.3f}, OVERWORLD.maxBlock(),
            null, null,
            null, null,
            null, null,
            false
    );
    public static final DimensionLightInfo END = new DimensionLightInfo(
            OVERWORLD.minBlock(), OVERWORLD.maxBlock(),
            null, null,
            null, null,
            null, new float[]{0.6f, 0.55f, 0.6f},
            false
    );

    static {
        MAP.put(DimensionTypes.OVERWORLD_ID, OVERWORLD);
        MAP.put(DimensionTypes.THE_NETHER_ID, NETHER);
        MAP.put(DimensionTypes.THE_END_ID, END);
    }

    public static DimensionLightInfo get(World world) {
        return get(world.getDimensionEntry().getKey().orElseThrow());
    }

    public static DimensionLightInfo get(RegistryKey<DimensionType> key) {
        return MAP.getOrDefault(key.getValue(), OVERWORLD);
    }
}

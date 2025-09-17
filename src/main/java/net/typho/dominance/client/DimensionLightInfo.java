package net.typho.dominance.client;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.LinkedHashMap;
import java.util.Map;

public record DimensionLightInfo(int[] minBlock, int[] maxBlock, boolean day) {
    public static final Map<Identifier, DimensionLightInfo> MAP = new LinkedHashMap<>();
    public static final DimensionLightInfo OVERWORLD = new DimensionLightInfo(new int[]{12, 10, 8}, new int[]{16, 15, 14}, true);
    public static final DimensionLightInfo NETHER = new DimensionLightInfo(new int[]{16, 15, 12}, OVERWORLD.maxBlock(), false);

    static {
        MAP.put(DimensionTypes.OVERWORLD_ID, OVERWORLD);
        MAP.put(DimensionTypes.THE_NETHER_ID, NETHER);
    }

    public static DimensionLightInfo get(World world) {
        return get(world.getDimensionEntry().getKey().orElseThrow());
    }

    public static DimensionLightInfo get(RegistryKey<DimensionType> key) {
        return MAP.getOrDefault(key.getValue(), OVERWORLD);
    }
}

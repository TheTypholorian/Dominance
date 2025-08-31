package net.typho.dominance;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Dominance implements ModInitializer {
    public static final String MOD_ID = "dominance";
    public static final EntityType<OrbEntity> ORB_ENTITY = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "orb"), EntityType.Builder.<OrbEntity>create(OrbEntity::new, SpawnGroup.MISC).dimensions(1f, 1f).build("orb"));

    @Override
    public void onInitialize() {
    }
}

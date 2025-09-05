package net.typho.dominance.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.typho.dominance.Dominance;

import java.util.concurrent.CompletableFuture;

public class GenArmorMaterialTags extends FabricTagProvider<ArmorMaterial> {
    public GenArmorMaterialTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.ARMOR_MATERIAL, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup lookup) {
        getOrCreateTagBuilder(Dominance.PIGLIN_NEUTRAL)
                .add(ArmorMaterials.GOLD.value())
                .add(Dominance.PIGLIN_MATERIAL.value());
    }
}

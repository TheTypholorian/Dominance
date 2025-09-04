package net.typho.dominance.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.typho.dominance.Dominance;

import java.util.concurrent.CompletableFuture;

public class GenRegistries extends FabricDynamicRegistryProvider {
    public GenRegistries(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        entries.addAll(registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT));
        entries.addAll(registries.getWrapperOrThrow(Dominance.REFORGE_KEY));
    }

    @Override
    public String getName() {
        return "";
    }
}

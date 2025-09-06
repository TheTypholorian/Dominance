package net.typho.dominance.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EnchantmentTags;
import net.typho.dominance.Dominance;

import java.util.concurrent.CompletableFuture;

public class GenEnchantmentTags extends FabricTagProvider.EnchantmentTagProvider {
    public GenEnchantmentTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(EnchantmentTags.DAMAGE_EXCLUSIVE_SET)
                .add(Dominance.BANE_OF_ILLAGERS);
        getOrCreateTagBuilder(Dominance.INFLICT_EXCLUSIVE_SET)
                .add(Dominance.EXPLODING)
                .add(Dominance.FREEZING)
                .add(Dominance.GRAVITY)
                .add(Dominance.WEAKENING);
        getOrCreateTagBuilder(Dominance.CONDITIONAL_EXCLUSIVE_SET)
                .add(Dominance.AMBUSH)
                .add(Dominance.COMMITTED)
                .add(Dominance.RAMPAGE);
        getOrCreateTagBuilder(Dominance.ROLL_EXCLUSIVE_SET)
                .add(Dominance.ACROBAT)
                .add(Dominance.FIRE_TRAIL)
                .add(Dominance.SWIFT_FOOTED);
        getOrCreateTagBuilder(EnchantmentTags.NON_TREASURE)
                .add(Dominance.AMBUSH)
                .add(Dominance.COMMITTED)
                .add(Dominance.COWARDICE)
                .add(Dominance.DYNAMO)
                .add(Dominance.EXPLODING)
                .add(Dominance.FREEZING)
                .add(Dominance.GRAVITY)
                .add(Dominance.BANE_OF_ILLAGERS)
                .add(Dominance.LEECHING)
                .add(Dominance.RAMPAGE)
                .add(Dominance.WEAKENING)
                .add(Dominance.ACROBAT)
                .add(Dominance.FIRE_TRAIL)
                .add(Dominance.RECKLESS)
                .add(Dominance.SWIFT_FOOTED);
    }
}

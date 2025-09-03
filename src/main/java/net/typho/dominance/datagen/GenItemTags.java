package net.typho.dominance.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.typho.dominance.Dominance;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class GenItemTags extends FabricTagProvider.ItemTagProvider {
    public GenItemTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture, @Nullable BlockTagProvider blockTagProvider) {
        super(output, completableFuture, blockTagProvider);
    }

    public GenItemTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup lookup) {
        getOrCreateTagBuilder(TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "tools/shields")))
                .add(Dominance.ROYAL_GUARD_SHIELD);
        getOrCreateTagBuilder(ItemTags.HEAD_ARMOR)
                .add(Dominance.ROYAL_GUARD_HELMET);
        getOrCreateTagBuilder(ItemTags.CHEST_ARMOR)
                .add(Dominance.ROYAL_GUARD_CHESTPLATE);
        getOrCreateTagBuilder(ItemTags.FOOT_ARMOR)
                .add(Dominance.ROYAL_GUARD_BOOTS);
        getOrCreateTagBuilder(ItemTags.DURABILITY_ENCHANTABLE)
                .add(Dominance.ROYAL_GUARD_MACE)
                .add(Dominance.GREAT_HAMMER);
        getOrCreateTagBuilder(ItemTags.SWORD_ENCHANTABLE)
                .addOptionalTag(ItemTags.AXES)
                .add(Dominance.ROYAL_GUARD_MACE)
                .add(Dominance.GREAT_HAMMER);
        getOrCreateTagBuilder(ItemTags.CROSSBOW_ENCHANTABLE)
                .addOptionalTag(ItemTags.BOW_ENCHANTABLE);
    }
}

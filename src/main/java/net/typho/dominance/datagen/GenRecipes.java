package net.typho.dominance.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.typho.dominance.Dominance;
import net.typho.dominance.gear.ReforgeRecipeJsonBuilder;

import java.util.concurrent.CompletableFuture;

public class GenRecipes extends FabricRecipeProvider {
    public GenRecipes(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ReforgeRecipeJsonBuilder.create(Ingredient.ofItems(Dominance.REFORGE_SMITHING_TEMPLATE), RecipeCategory.COMBAT)
                .criterion("has_reforge_smithing_template", conditionsFromItem(Dominance.REFORGE_SMITHING_TEMPLATE))
                .offerTo(exporter, Dominance.id("reforge"));
    }
}

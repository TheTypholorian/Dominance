package net.typho.dominance.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.typho.dominance.Dominance;
import net.typho.dominance.gear.Salvageable;

import java.util.concurrent.CompletableFuture;

public class GenRecipes extends FabricRecipeProvider {
    public GenRecipes(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    public static void offerReforgeRecipe(RecipeExporter exporter, Item input, RecipeCategory category) {
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(Dominance.REFORGE_SMITHING_TEMPLATE), Ingredient.ofItems(input), Ingredient.empty(), category, input
                )
                .criterion("has_reforge_smithing_template", conditionsFromItem(Dominance.REFORGE_SMITHING_TEMPLATE))
                .offerTo(exporter, getItemPath(input) + "_reforging");
    }

    @Override
    public void generate(RecipeExporter exporter) {
        for (Item item : Registries.ITEM) {
            if (item instanceof Salvageable) {
                offerReforgeRecipe(exporter, item, RecipeCategory.COMBAT);
            }
        }
    }
}

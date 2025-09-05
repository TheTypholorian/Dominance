package net.typho.dominance.gear;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReforgeRecipeJsonBuilder {
	private final Ingredient template;
	private final RecipeCategory category;
	private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();

	public ReforgeRecipeJsonBuilder(Ingredient template, RecipeCategory category) {
		this.category = category;
		this.template = template;
	}

	public static ReforgeRecipeJsonBuilder create(Ingredient template, RecipeCategory category) {
		return new ReforgeRecipeJsonBuilder(template, category);
	}

	public ReforgeRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
		criteria.put(name, criterion);
		return this;
	}

	public void offerTo(RecipeExporter exporter, String recipeId) {
		offerTo(exporter, Identifier.of(recipeId));
	}

	public void offerTo(RecipeExporter exporter, Identifier recipeId) {
		validate(recipeId);
		Advancement.Builder builder = exporter.getAdvancementBuilder()
			.criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
			.rewards(AdvancementRewards.Builder.recipe(recipeId))
			.criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
		criteria.forEach(builder::criterion);
		ReforgeRecipe recipe = new ReforgeRecipe(template);
		exporter.accept(recipeId, recipe, builder.build(recipeId.withPrefixedPath("recipes/" + category.getName() + "/")));
	}

	private void validate(Identifier recipeId) {
		if (criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + recipeId);
		}
	}
}

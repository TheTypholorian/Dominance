package net.typho.dominance.gear;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import net.typho.dominance.Dominance;

import java.util.Objects;

public class ReforgeRecipe implements SmithingRecipe {
    public final Ingredient template;

    public ReforgeRecipe(Ingredient template) {
        this.template = template;
    }

    @Override
    public boolean testTemplate(ItemStack stack) {
        return template.test(stack);
    }

    @Override
    public boolean testBase(ItemStack stack) {
        return true;
    }

    @Override
    public boolean testAddition(ItemStack stack) {
        return stack.isEmpty();
    }

    @Override
    public boolean matches(SmithingRecipeInput input, World world) {
        return testTemplate(input.template()) && testBase(input.base()) && Reforge.stackCanReforge(input.base(), world.getRegistryManager()) && testAddition(input.addition());
    }

    @Override
    public ItemStack craft(SmithingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack stack = input.base().copy();
        stack.set(Dominance.REFORGE_COMPONENT, Objects.requireNonNull(Reforge.pickForStack(stack, lookup)).generate(stack));
        return stack;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Dominance.REFORGE_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<ReforgeRecipe> {
        private static final MapCodec<ReforgeRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Ingredient.ALLOW_EMPTY_CODEC.fieldOf("template").forGetter(recipe -> recipe.template)
                        )
                        .apply(instance, ReforgeRecipe::new)
        );
        public static final PacketCodec<RegistryByteBuf, ReforgeRecipe> PACKET_CODEC = PacketCodec.ofStatic(
                ReforgeRecipe.Serializer::write, ReforgeRecipe.Serializer::read
        );

        @Override
        public MapCodec<ReforgeRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, ReforgeRecipe> packetCodec() {
            return PACKET_CODEC;
        }

        private static ReforgeRecipe read(RegistryByteBuf buf) {
            Ingredient ingredient = Ingredient.PACKET_CODEC.decode(buf);
            return new ReforgeRecipe(ingredient);
        }

        private static void write(RegistryByteBuf buf, ReforgeRecipe recipe) {
            Ingredient.PACKET_CODEC.encode(buf, recipe.template);
        }
    }
}

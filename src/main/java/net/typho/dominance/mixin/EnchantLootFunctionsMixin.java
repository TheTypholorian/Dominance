package net.typho.dominance.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.loot.function.EnchantWithLevelsLootFunction;
import net.minecraft.util.math.random.Random;
import net.typho.dominance.Dominance;
import net.typho.dominance.gear.Reforge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EnchantRandomlyLootFunction.class, EnchantWithLevelsLootFunction.class})
public class EnchantLootFunctionsMixin {
    @Inject(
            method = "process",
            at = @At("TAIL")
    )
    private static void process(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir, @Local Random random) {
        Reforge.Factory<?> reforge = Reforge.pickForStack(stack, context.getWorld().getRegistryManager(), random);

        if (reforge != null) {
            stack.set(Dominance.REFORGE_COMPONENT, reforge.generate(stack));
        }
    }
}

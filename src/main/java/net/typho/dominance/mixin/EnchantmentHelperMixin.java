package net.typho.dominance.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.typho.dominance.gear.BurstCrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @ModifyReturnValue(
            method = "getProjectileCount",
            at = @At("TAIL")
    )
    private static int getProjectileCount(int original, @Local(argsOnly = true) ItemStack stack) {
        return stack.getItem() instanceof BurstCrossbowItem burst ? burst.extraProjectiles() + original : original;
    }
}

package net.typho.dominance.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.typho.dominance.Dominance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneScreenHandlerMixin {
    @WrapOperation(
            method = "getOutputStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;hasEnchantments(Lnet/minecraft/item/ItemStack;)Z"
            )
    )
    private boolean hasEnchantments(ItemStack stack, Operation<Boolean> original) {
        return original.call(stack) || stack.isIn(Dominance.ANY_REFORGABLE);
    }

    @WrapOperation(
            method = "getOutputStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/GrindstoneScreenHandler;grind(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private ItemStack grind(GrindstoneScreenHandler instance, ItemStack stack, Operation<ItemStack> original) {
        if (!stack.hasEnchantments() && stack.isIn(Dominance.ANY_REFORGABLE)) {
            int count = switch (stack.getRarity()) {
                case COMMON, UNCOMMON -> 1;
                case RARE -> 2;
                case EPIC -> 3;
            };

            return new ItemStack(Dominance.REFORGE_SMITHING_TEMPLATE, count);
        }

        return original.call(instance, stack);
    }
}

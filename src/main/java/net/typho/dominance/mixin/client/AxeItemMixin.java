package net.typho.dominance.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.typho.dominance.Dominance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AxeItem.class)
public class AxeItemMixin {
    @WrapOperation(
            method = "shouldCancelStripAttempt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"
            )
    )
    private static boolean isOf(ItemStack instance, Item item, Operation<Boolean> original) {
        return item == Items.SHIELD ? original.call(instance, item) || original.call(instance, Dominance.ROYAL_GUARD_SHIELD) : original.call(instance, item);
    }
}

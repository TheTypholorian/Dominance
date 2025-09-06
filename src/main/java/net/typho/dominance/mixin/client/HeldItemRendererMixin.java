package net.typho.dominance.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.typho.dominance.Dominance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @WrapOperation(
            method = {
                    "getHandRenderType",
                    "getUsingItemHandRenderType",
                    "isChargedCrossbow",
                    "renderFirstPersonItem"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"
            )
    )
    private static boolean isShield(ItemStack instance, Item item, Operation<Boolean> original) {
        return item == Items.SHIELD ? instance.isIn(Dominance.CROSSBOWS) || original.call(instance, item) : original.call(instance, item);
    }
}

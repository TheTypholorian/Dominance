package net.typho.dominance.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerItemCooldownManager;
import net.typho.dominance.gear.ConsumesSouls;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCooldownManager.class)
public class ItemCooldownManagerMixin {
    @Inject(
            method = "isCoolingDown",
            at = @At("HEAD"),
            cancellable = true
    )
    private void isCoolingDown(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (item instanceof ConsumesSouls consumes) {
            if ((Object) this instanceof ServerItemCooldownManager server) {
                if (!consumes.hasEnoughSouls(server.player)) {
                    cir.setReturnValue(true);
                }
            } else {
                if (!consumes.hasEnoughSouls(MinecraftClient.getInstance().player)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}

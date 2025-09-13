package net.typho.dominance.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> {
    @Shadow
    protected abstract boolean isPointOverSlot(Slot slot, double pointX, double pointY);

    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V"
            )
    )
    private void drawSlot(HandledScreen<T> instance, DrawContext context, Slot slot, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) int mouseX, @Local(argsOnly = true, ordinal = 1) int mouseY) {
        if (isPointOverSlot(slot, mouseX, mouseY) && slot.isEnabled() && slot.canBeHighlighted()) {
            MatrixStack stack = context.getMatrices();
            stack.push();
            stack.peek().getPositionMatrix()
                    .scaleAround(1.5f, slot.x + 8, slot.y + 8, 0)
                    .translate(-(slot.x + 8 - mouseX + x) / 8f, -(slot.y + 8 - mouseY + y) / 8f, 10);
            original.call(instance, context, slot);
            stack.pop();
        } else {
            original.call(instance, context, slot);
        }
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlight(Lnet/minecraft/client/gui/DrawContext;III)V"
            )
    )
    private void drawSlotHighlight(DrawContext context, int x, int y, int z) {
    }
}

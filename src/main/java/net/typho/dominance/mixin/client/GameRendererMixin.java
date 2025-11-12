package net.typho.dominance.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.typho.dominance.Dominance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    MinecraftClient client;

    @Inject(
            method = "bobView",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bobView(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (client.getCameraEntity() instanceof PlayerEntity player && Dominance.PLAYER_DATA.get(player).isRolling()) {
            ci.cancel();
        }
    }
}

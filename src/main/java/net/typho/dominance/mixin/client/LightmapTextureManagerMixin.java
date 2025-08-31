package net.typho.dominance.mixin.client;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.typho.dominance.client.DominanceClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Shadow
    @Final
    private NativeImage image;

    /*
    @ModifyArg(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/LightmapTextureManager;getBrightness(Lnet/minecraft/world/dimension/DimensionType;I)F",
                    ordinal = 0
            ),
            index = 1
    )
    private int updateSky(int lightLevel) {
        float f = lightLevel / 15f;
        f *= f;
        return (int) (f * f * 12);
    }

    @ModifyArg(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/LightmapTextureManager;getBrightness(Lnet/minecraft/world/dimension/DimensionType;I)F",
                    ordinal = 1
            ),
            index = 1
    )
    private int updateBlock(int lightLevel) {
        return lightLevel - 4;
    }
     */

    @Inject(
            method = "update",
            at = @At("TAIL")
    )
    private void update(float delta, CallbackInfo ci) {
        if (DominanceClient.SAVE_LIGHTMAP.isPressed()) {
            try {
                image.writeTo(new File("lightmap.png"));
                System.out.println("Saved lightmap to lightmap.png");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

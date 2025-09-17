package net.typho.dominance.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
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
    public NativeImage image;

    @Shadow
    private boolean dirty;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow @Final private NativeImageBackedTexture texture;

    @Inject(
            method = "update",
            at = @At("HEAD"),
            cancellable = true
    )
    private void updateHead(float delta, CallbackInfo ci) {
        if (dirty) {
            dirty = false;

            client.getProfiler().push("lightTex");

            if (client.world != null) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        image.setColor(x, y, 0xFF000000 | (x << 12) | (y << 4));
                    }
                }
            }

            texture.upload();
            client.getProfiler().pop();

            ci.cancel();
        }
    }

    @Inject(
            method = "update",
            at = @At("TAIL")
    )
    private void updateTail(float delta, CallbackInfo ci) {
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

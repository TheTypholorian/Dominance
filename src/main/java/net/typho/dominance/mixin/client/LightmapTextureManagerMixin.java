package net.typho.dominance.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.math.MathHelper;
import net.typho.dominance.client.DimensionLightInfo;
import net.typho.dominance.client.DominanceClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.io.IOException;

@Mixin(value = LightmapTextureManager.class, priority = 0)
public class LightmapTextureManagerMixin {
    @Shadow
    @Final
    public NativeImage image;

    @Shadow
    private boolean dirty;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private NativeImageBackedTexture texture;

    @WrapMethod(
            method = "update"
    )
    private void updateHead(float delta, Operation<Void> original) {
        if (dirty) {
            dirty = false;

            client.getProfiler().push("lightTex");

            if (client.world != null) {
                DimensionLightInfo dimLight = DimensionLightInfo.get(client.world);

                for (int sky = 1; sky <= 16; sky++) {
                    float timeDelta = (sky - 1) / 15f;

                    if (dimLight.day()) {
                        timeDelta *= (float) Math.sin((float) client.world.getTimeOfDay() / 12000 * Math.PI) / 2 + 0.5f;
                    }

                    int[] blockScales = {
                            MathHelper.lerp(timeDelta, dimLight.minBlock()[0], dimLight.maxBlock()[0]),
                            MathHelper.lerp(timeDelta, dimLight.minBlock()[1], dimLight.maxBlock()[1]),
                            MathHelper.lerp(timeDelta, dimLight.minBlock()[2], dimLight.maxBlock()[2])
                    };

                    for (int block = 1; block <= 16; block++) {
                        int red = MathHelper.clamp(block * blockScales[0], 0, 255);
                        int green = MathHelper.clamp(block * blockScales[1], 0, 255);
                        int blue = MathHelper.clamp(block * blockScales[2], 0, 255);
                        image.setColor(block - 1, sky - 1, 0xFF000000 | (blue << 16) | (green << 8) | red);
                    }
                }
            }

            texture.upload();
            client.getProfiler().pop();
        }

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

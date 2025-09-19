package net.typho.dominance.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.typho.dominance.TempAndHumidity;
import net.typho.dominance.client.DominanceClient;
import org.spongepowered.asm.mixin.*;

import java.io.File;
import java.io.IOException;

@Mixin(value = LightmapTextureManager.class, priority = 0)
@Implements(@Interface(iface = TempAndHumidity.class, prefix = "tah$"))
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

    @Unique
    private float temp, humid;

    public float tah$temp() {
        return temp;
    }

    public float tah$humid() {
        return humid;
    }

    @WrapMethod(
            method = "update"
    )
    private void update(float delta, Operation<Void> original) {
        if (client.world != null && client.player != null) {
            Biome biome = client.world.getBiome(client.player.getBlockPos()).value();

            temp = MathHelper.lerp(delta / 50, temp, biome.getTemperature());
            humid = MathHelper.lerp(delta / 50, humid, biome.weather.downfall());

            if (dirty) {
                dirty = false;

                client.getProfiler().push("lightTex");

                if (client.world != null && client.player != null) {
                    DominanceClient.createLightmap(client.world, client.player, image, temp, humid);
                }

                texture.upload();
                client.getProfiler().pop();

                if (DominanceClient.SAVE_LIGHTMAP.isPressed()) {
                    try (NativeImage big = new NativeImage(1024, 1024, false)) {
                        DominanceClient.createLightmap(client.world, client.player, big, temp, humid);
                        big.writeTo(new File("lightmap.png"));
                        System.out.println("Saved lightmap to lightmap.png");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}

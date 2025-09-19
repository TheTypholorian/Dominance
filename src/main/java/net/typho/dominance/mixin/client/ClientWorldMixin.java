package net.typho.dominance.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.typho.dominance.TempAndHumidity;
import net.typho.dominance.client.DimensionLightInfo;
import net.typho.dominance.client.DominanceClient;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {
    @Shadow
    @Final
    private MinecraftClient client;

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @ModifyReturnValue(
            method = "getSkyColor",
            at = @At("RETURN")
    )
    private Vec3d getSkyColor(Vec3d original) {
        Vector3f vec = DominanceClient.getTempTint(DimensionLightInfo.get(this), ((TempAndHumidity) client.gameRenderer.getLightmapTextureManager()).temp());
        return original.lerp(new Vec3d(vec.x * vec.x, vec.y * vec.y, vec.z * vec.z), 0.3).multiply(DominanceClient.getDay(this));
    }
}

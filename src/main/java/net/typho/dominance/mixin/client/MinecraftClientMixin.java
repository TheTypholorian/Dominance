package net.typho.dominance.mixin.client;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.typho.dominance.Dominance;
import net.typho.dominance.client.DominanceClient;
import net.typho.dominance.client.RoyalGuardStatueBlockEntityRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Final
    private ReloadableResourceManagerImpl resourceManager;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void init(RunArgs args, CallbackInfo ci) {
        resourceManager.registerReloader(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Dominance.id("royal_guard_statue");
            }

            @Override
            public void reload(ResourceManager manager) {
                manager.getResource(Dominance.id("models/block/royal_guard_statue.obj")).ifPresentOrElse(res -> {
                    try {
                        RoyalGuardStatueBlockEntityRenderer.MODEL = DominanceClient.loadObj(new String(res.getInputStream().readAllBytes()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, () -> {
                    throw new NullPointerException("No royal guard statue .obj file");
                });
            }
        });
    }
}

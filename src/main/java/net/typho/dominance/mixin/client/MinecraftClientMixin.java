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
import net.typho.dominance.gear.CorruptedBeaconItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
                return Dominance.id("obj_model_loader");
            }

            @Override
            public void reload(ResourceManager manager) {
                RoyalGuardStatueBlockEntityRenderer.MODEL = DominanceClient.loadObj(manager, Dominance.id("models/block/royal_guard_statue.obj"));
                CorruptedBeaconItem.BEAM_MODEL = DominanceClient.loadObj(manager, Dominance.id("models/item/corrupted_beacon_beam.obj"));
            }
        });
    }
}

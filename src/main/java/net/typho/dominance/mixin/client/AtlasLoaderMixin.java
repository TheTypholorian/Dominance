package net.typho.dominance.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.texture.atlas.AtlasLoader;
import net.minecraft.client.texture.atlas.AtlasSource;
import net.minecraft.client.texture.atlas.SingleAtlasSource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.typho.dominance.Dominance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(AtlasLoader.class)
public class AtlasLoaderMixin {
    @Inject(
            method = "of",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"
            )
    )
    private static void addAll(ResourceManager resourceManager, Identifier id, CallbackInfoReturnable<AtlasLoader> cir, @Local List<AtlasSource> list) {
        if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && id.getPath().equals("shield_patterns")) {
            list.add(new SingleAtlasSource(Dominance.id("entity/royal_guard_shield"), Optional.empty()));
        }
    }
}

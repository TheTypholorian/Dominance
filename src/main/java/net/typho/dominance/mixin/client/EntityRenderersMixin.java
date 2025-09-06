package net.typho.dominance.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderers.class)
public class EntityRenderersMixin {
    @WrapOperation(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/EntityRenderers;register(Lnet/minecraft/entity/EntityType;Lnet/minecraft/client/render/entity/EntityRendererFactory;)V"
            )
    )
    private static <T extends Entity> void register(EntityType<? extends T> type, EntityRendererFactory<T> factory, Operation<Void> original) {
        if (type != EntityType.VINDICATOR) {
            original.call(type, factory);
        }
    }
}

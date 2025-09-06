package net.typho.dominance.mixin.client;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityModels.class)
public class EntityModelsMixin {
    @WrapOperation(
            method = "getModels",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMap$Builder;"
            ),
            remap = false
    )
    private static ImmutableMap.Builder<?, ?> register(ImmutableMap.Builder<?, ?> instance, Object key, Object value, Operation<ImmutableMap.Builder<?, ?>> original) {
        return key == EntityModelLayers.VINDICATOR ? instance : original.call(instance, key, value);
    }
}

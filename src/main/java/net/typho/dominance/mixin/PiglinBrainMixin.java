package net.typho.dominance.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.registry.entry.RegistryEntry;
import net.typho.dominance.Dominance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PiglinBrain.class)
public class PiglinBrainMixin {
    @WrapOperation(
            method = "wearsGoldArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/registry/entry/RegistryEntry;matches(Lnet/minecraft/registry/entry/RegistryEntry;)Z"
            )
    )
    private static boolean wearsGoldArmor(RegistryEntry<ArmorMaterial> instance, RegistryEntry<ArmorMaterial> gold, Operation<Boolean> original) {
        return original.call(instance, gold) || instance.isIn(Dominance.PIGLIN_NEUTRAL);
    }
}

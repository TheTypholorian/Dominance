package net.typho.dominance.mixin.client;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {
    @ModifyVariable(
            method = "<init>",
            at = @At("CTOR_HEAD"),
            ordinal = 1,
            argsOnly = true
    )
    private boolean hasSkylight(boolean value) {
        return true;
    }
}

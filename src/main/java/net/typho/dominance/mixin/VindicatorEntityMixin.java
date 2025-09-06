package net.typho.dominance.mixin;

import net.minecraft.entity.mob.VindicatorEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(VindicatorEntity.class)
public class VindicatorEntityMixin {
    @ModifyConstant(
            method = "createVindicatorAttributes",
            constant = @Constant(doubleValue = 24)
    )
    private static double maxHealth(double constant) {
        return constant + 8;
    }

    @ModifyConstant(
            method = "createVindicatorAttributes",
            constant = @Constant(doubleValue = 5)
    )
    private static double damage(double damage) {
        return damage - 2;
    }
}

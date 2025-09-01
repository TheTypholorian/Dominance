package net.typho.dominance.mixin;

import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
    @ModifyConstant(
            method = "tick",
            constant = @Constant(doubleValue = 0.1)
    )
    private double speed(double constant) {
        return constant * 3;
    }

    @ModifyConstant(
            method = "expensiveUpdate",
            constant = @Constant(doubleValue = 64)
    )
    private double squaredRange(double constant) {
        return constant * 4;
    }

    @ModifyConstant(
            method = "expensiveUpdate",
            constant = @Constant(doubleValue = 8)
    )
    private double range(double constant) {
        return constant * 2;
    }

    @ModifyConstant(
            method = "onPlayerCollision",
            constant = @Constant(intValue = 2)
    )
    private int cooldown(int constant) {
        return constant / 2;
    }
}

package net.typho.dominance.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.typho.dominance.Dominance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyReturnValue(
            method = "createPlayerAttributes",
            at = @At("RETURN")
    )
    private static DefaultAttributeContainer.Builder createPlayerAttributes(DefaultAttributeContainer.Builder builder) {
        return builder.add(Dominance.PLAYER_ROLL_COOLDOWN)
                .add(Dominance.PLAYER_ROLL_LENGTH)
                .add(Dominance.PLAYER_FIRE_TRAIL)
                .add(Dominance.PLAYER_SWIFT_FOOTED)
                .add(Dominance.PLAYER_MAX_SOULS);
    }
}

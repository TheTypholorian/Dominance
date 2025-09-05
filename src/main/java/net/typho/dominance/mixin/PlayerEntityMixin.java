package net.typho.dominance.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.typho.dominance.Dominance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
                .add(Dominance.PLAYER_SWIFT_FOOTED);
    }

    @Inject(
            method = "attack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void attack(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (target.isAttackable() && !target.handleAttack(player)) {
            Dominance.attack(player, target);
        }

        ci.cancel();
    }
}

package net.typho.dominance.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.typho.dominance.Dominance;
import net.typho.dominance.client.DamageParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private void attack(Entity target, CallbackInfo ci, @Local(ordinal = 3) float f) {
        Vec3d pos = target.getEyePos().add(getEyePos().subtract(target.getEyePos()).normalize().multiply(0.5));
        target.getWorld().addParticle(new DamageParticleEffect(Dominance.DAMAGE_PARTICLE, f), pos.x, pos.y, pos.z, 0, 0, 0);
    }
}

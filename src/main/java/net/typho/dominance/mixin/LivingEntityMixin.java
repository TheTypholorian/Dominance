package net.typho.dominance.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.typho.dominance.client.DamageNumberParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "applyDamage",
            at = @At("HEAD")
    )
    private void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        System.out.println(source + " " + getWorld().isClient);
        if (source.getAttacker() instanceof PlayerEntity && getWorld().isClient) {
            System.out.println("spawn particle");
            DamageNumberParticle.PARTICLES.add(new DamageNumberParticle(getPos().add(0, 2, 0), amount));
        }
    }
}

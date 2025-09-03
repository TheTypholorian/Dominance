package net.typho.dominance.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.typho.dominance.gear.HuntingBowItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin {
    @Shadow
    @Nullable
    public abstract Entity getOwner();

    @Inject(
            method = "onEntityHit",
            at = @At("TAIL")
    )
    private void onEntityHit(EntityHitResult hit, CallbackInfo ci) {
        if (hit.getEntity() instanceof LivingEntity living) {
            Entity owner = getOwner();

            if (owner != null) {
                ItemStack weapon = owner.getWeaponStack();

                if (weapon != null && weapon.getItem() instanceof HuntingBowItem) {
                    for (Entity entity : owner.getWorld().getOtherEntities(hit.getEntity(), Box.from(owner.getPos()).expand(16))) {
                        if (entity instanceof Tameable tameable && entity instanceof MobEntity mob && Objects.equals(tameable.getOwnerUuid(), owner.getUuid())) {
                            mob.setTarget(living);
                        }
                    }
                }
            }
        }
    }
}

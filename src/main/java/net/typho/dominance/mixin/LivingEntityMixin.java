package net.typho.dominance.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.typho.dominance.Dominance;
import net.typho.dominance.enchants.EnchantmentPostKillEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean isDead();

    @Inject(
            method = "applyDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;)V"
            )
    )
    private void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (isDead() && source.getWeaponStack() != null && getWorld() instanceof ServerWorld world) {
            EnchantmentHelper.forEachEnchantment(source.getWeaponStack(), (enchantment, level) -> {
                for (EnchantmentEffectEntry<EnchantmentPostKillEffect> entry : enchantment.value().getEffect(Dominance.POST_KILL)) {
                    entry.effect().apply(world, level, source.getWeaponStack(), this, source, getPos());
                }
            });
        }
    }
}

package net.typho.dominance.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.typho.dominance.DamageParticleS2C;
import net.typho.dominance.Dominance;
import net.typho.dominance.enchants.EnchantmentPostKillEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.List;

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

        if (source.getAttacker() instanceof PlayerEntity attacker) {
            if (!getWorld().isClient) {
                List<ServerPlayerEntity> players = new LinkedList<>();
                Box box = Box.from(attacker.getPos()).expand(64);
                ((ServerWorld) getWorld()).collectEntitiesByType(TypeFilter.instanceOf(ServerPlayerEntity.class), e -> e == attacker || box.contains(e.getPos()), players);
                CustomPayload payload = new DamageParticleS2C(this, attacker, amount);

                for (ServerPlayerEntity player : players) {
                    ServerPlayNetworking.send(player, payload);
                }
            }
        }
    }
}

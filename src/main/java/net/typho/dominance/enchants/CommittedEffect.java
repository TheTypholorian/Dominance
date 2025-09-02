package net.typho.dominance.enchants;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record CommittedEffect() implements EnchantmentModifyDamageEffect {
    public static final MapCodec<CommittedEffect> CODEC = MapCodec.unit(CommittedEffect::new);

    @Override
    public float apply(ServerWorld world, int level, ItemStack stack, float damage, Entity victim, DamageSource source, Vec3d pos) {
        if (victim instanceof LivingEntity living) {
            return damage * (1 + (1 - (living.getHealth() / living.getMaxHealth())) * ((float) level / 3));
        } else {
            return damage;
        }
    }

    @Override
    public MapCodec<? extends EnchantmentModifyDamageEffect> getCodec() {
        return CODEC;
    }
}

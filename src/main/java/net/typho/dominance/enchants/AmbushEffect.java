package net.typho.dominance.enchants;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record AmbushEffect() implements EnchantmentModifyDamageEffect {
    public static final MapCodec<AmbushEffect> CODEC = MapCodec.unit(AmbushEffect::new);

    @Override
    public DamageModifier apply(ServerWorld world, int level, ItemStack stack, Entity victim, DamageSource source, Vec3d pos) {
        if (victim instanceof MobEntity mob && mob.getTarget() != source.getAttacker()) {
            return new DamageModifier(1 + level / 5f, DamageModifier.Operation.MULTIPLY);
        }

        return null;
    }

    @Override
    public MapCodec<? extends EnchantmentModifyDamageEffect> getCodec() {
        return CODEC;
    }
}

package net.typho.dominance.enchants;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record CowardiceEffect() implements EnchantmentModifyDamageEffect {
    public static final MapCodec<CowardiceEffect> CODEC = MapCodec.unit(CowardiceEffect::new);

    @Override
    public DamageModifier apply(ServerWorld world, int level, ItemStack stack, Entity victim, DamageSource source, Vec3d pos) {
        if (source.getAttacker() instanceof LivingEntity attacker && attacker.getHealth() >= attacker.getMaxHealth()) {
            return new DamageModifier(1.1f + level / 10f, DamageModifier.Operation.MULTIPLY);
        }

        return null;
    }

    @Override
    public MapCodec<? extends EnchantmentModifyDamageEffect> getCodec() {
        return CODEC;
    }
}

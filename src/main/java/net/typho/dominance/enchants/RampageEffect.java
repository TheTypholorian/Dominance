package net.typho.dominance.enchants;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.typho.dominance.Dominance;

public record RampageEffect() implements EnchantmentPostKillEffect {
    public static final MapCodec<RampageEffect> CODEC = MapCodec.unit(RampageEffect::new);

    @Override
    public void apply(ServerWorld world, int level, ItemStack stack, Entity victim, DamageSource source, Vec3d pos) {
        if (source.getAttacker() instanceof LivingEntity attacker && world.random.nextInt(2) == 0) {
            attacker.addStatusEffect(new StatusEffectInstance(Dominance.RAMPAGE_STATUS_EFFECT, level * 100));
        }
    }

    @Override
    public MapCodec<? extends EnchantmentPostKillEffect> getCodec() {
        return CODEC;
    }
}

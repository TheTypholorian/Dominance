package net.typho.dominance.enchants;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public record GravityEffect() implements EnchantmentEntityEffect {
    public static final MapCodec<GravityEffect> CODEC = MapCodec.unit(GravityEffect::new);

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity victim, Vec3d pos) {
        for (Entity entity : victim.getWorld().getOtherEntities(victim, Box.from(victim.getPos()).expand(level + 1))) {
            entity.addVelocity(victim.getPos().subtract(entity.getPos()).normalize().multiply(0.2));
            entity.velocityModified = true;
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}

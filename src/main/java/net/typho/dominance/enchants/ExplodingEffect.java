package net.typho.dominance.enchants;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public record ExplodingEffect() implements EnchantmentPostKillEffect {
    public static final MapCodec<ExplodingEffect> CODEC = MapCodec.unit(ExplodingEffect::new);

    @Override
    public void apply(ServerWorld world, int level, ItemStack stack, Entity victim, DamageSource source, Vec3d pos) {
        world.createExplosion(victim, pos.x, pos.y, pos.z, level, World.ExplosionSourceType.NONE);
    }

    @Override
    public MapCodec<? extends EnchantmentPostKillEffect> getCodec() {
        return CODEC;
    }
}

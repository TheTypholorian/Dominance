package net.typho.dominance.enchants;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.typho.dominance.Dominance;

import java.util.function.Function;

public interface EnchantmentPostKillEffect {
    Codec<EnchantmentPostKillEffect> CODEC = Dominance.ENCHANTMENT_POST_KILL_EFFECTS
            .getCodec()
            .dispatch(EnchantmentPostKillEffect::getCodec, Function.identity());

    void apply(ServerWorld world, int level, ItemStack stack, Entity victim, DamageSource source, Vec3d pos);

    MapCodec<? extends EnchantmentPostKillEffect> getCodec();
}

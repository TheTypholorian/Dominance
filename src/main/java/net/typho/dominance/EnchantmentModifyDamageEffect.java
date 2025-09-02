package net.typho.dominance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.function.Function;

public interface EnchantmentModifyDamageEffect {
    Codec<EnchantmentModifyDamageEffect> CODEC = Dominance.ENCHANTMENT_ATTACK_EFFECTS
            .getCodec()
            .dispatch(EnchantmentModifyDamageEffect::getCodec, Function.identity());

    float apply(ServerWorld world, int level, ItemStack stack, float damage, Entity victim, DamageSource source, Vec3d pos);

    MapCodec<? extends EnchantmentModifyDamageEffect> getCodec();
}

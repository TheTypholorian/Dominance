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

public interface EnchantmentModifyDamageEffect {
    Codec<EnchantmentModifyDamageEffect> CODEC = Dominance.ENCHANTMENT_DAMAGE_EFFECTS
            .getCodec()
            .dispatch(EnchantmentModifyDamageEffect::getCodec, Function.identity());

    DamageModifier apply(ServerWorld world, int level, ItemStack stack, Entity victim, DamageSource source, Vec3d pos);

    MapCodec<? extends EnchantmentModifyDamageEffect> getCodec();
}

package net.typho.dominance.enchants;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record LeechingEffect() implements EnchantmentPostKillEffect {
    public static final MapCodec<LeechingEffect> CODEC = MapCodec.unit(LeechingEffect::new);

    @Override
    public void apply(ServerWorld world, int level, ItemStack stack, Entity victim, DamageSource source, Vec3d pos) {
        if (source.getAttacker() instanceof LivingEntity attacker && victim instanceof LivingEntity living) {
            System.out.println("heal");
            attacker.heal(living.getMaxHealth() * (0.05f + (level - 1) * 0.02f));
        }
    }

    @Override
    public MapCodec<? extends EnchantmentPostKillEffect> getCodec() {
        return CODEC;
    }
}

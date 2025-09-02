package net.typho.dominance.enchants;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.typho.dominance.Dominance;
import net.typho.dominance.DominancePlayerData;

public record DynamoEffect() implements EnchantmentModifyDamageEffect {
    public static final MapCodec<DynamoEffect> CODEC = MapCodec.unit(DynamoEffect::new);

    @Override
    public float apply(ServerWorld world, int level, ItemStack stack, float damage, Entity victim, DamageSource source, Vec3d pos) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            DominancePlayerData data = Dominance.PLAYER_DATA.getNullable(player);

            if (data != null && data.hasDynamo()) {
                data.setDynamo(false);
                return damage * (2 + (level - 1) / 4f);
            }
        }

        return damage;
    }

    @Override
    public MapCodec<? extends EnchantmentModifyDamageEffect> getCodec() {
        return CODEC;
    }
}

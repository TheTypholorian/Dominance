package net.typho.dominance.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class DamageParticleEffect implements ParticleEffect {
    public final ParticleType<?> type;
    public final float damage;

    public DamageParticleEffect(ParticleType<?> type, float damage) {
        this.type = type;
        this.damage = damage;
    }

    public static MapCodec<DamageParticleEffect> createCodec(ParticleType<DamageParticleEffect> type) {
        return Codec.FLOAT.xmap(damage -> new DamageParticleEffect(type, damage), effect -> effect.damage)
                .fieldOf("damage");
    }

    public static PacketCodec<? super RegistryByteBuf, DamageParticleEffect> createPacketCodec(ParticleType<DamageParticleEffect> type) {
        return PacketCodecs.FLOAT.xmap(damage -> new DamageParticleEffect(type, damage), effect -> effect.damage);
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}

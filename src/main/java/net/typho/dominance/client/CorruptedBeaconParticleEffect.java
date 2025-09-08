package net.typho.dominance.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public class CorruptedBeaconParticleEffect implements ParticleEffect {
    public final ParticleType<?> type;
    public final Vec3d target;

    public CorruptedBeaconParticleEffect(ParticleType<?> type, Vec3d target) {
        this.type = type;
        this.target = target;
    }

    public static MapCodec<CorruptedBeaconParticleEffect> createCodec(ParticleType<CorruptedBeaconParticleEffect> type) {
        return Vec3d.CODEC.xmap(target -> new CorruptedBeaconParticleEffect(type, target), effect -> effect.target)
                .fieldOf("target");
    }

    public static PacketCodec<? super RegistryByteBuf, CorruptedBeaconParticleEffect> createPacketCodec(ParticleType<CorruptedBeaconParticleEffect> type) {
        return PacketCodec.tuple(
                PacketCodecs.DOUBLE, effect -> effect.target.x,
                PacketCodecs.DOUBLE, effect -> effect.target.y,
                PacketCodecs.DOUBLE, effect -> effect.target.z,
                (x, y, z) -> new CorruptedBeaconParticleEffect(type, new Vec3d(x, y, z));
        );
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}

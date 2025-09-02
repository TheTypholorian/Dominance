package net.typho.dominance;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record DamageParticleS2C(Vec3d pos, float damage) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, DamageParticleS2C> PACKET_CODEC = new PacketCodec<>() {
        @Override
        public DamageParticleS2C decode(PacketByteBuf buf) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            float dmg = buf.readFloat();
            return new DamageParticleS2C(new Vec3d(x, y, z), dmg);
        }

        @Override
        public void encode(PacketByteBuf buf, DamageParticleS2C pkt) {
            Vec3d p = pkt.pos();
            buf.writeDouble(p.x);
            buf.writeDouble(p.y);
            buf.writeDouble(p.z);
            buf.writeFloat(pkt.damage());
        }
    };
    public static final Id<DamageParticleS2C> ID = new Id<>(Identifier.of(Dominance.MOD_ID, "damage_particle"));

    public DamageParticleS2C(Entity target, Entity attacker, float damage) {
        this(target.getEyePos().add(attacker.getEyePos().subtract(target.getEyePos()).normalize().multiply(0.5)), damage);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

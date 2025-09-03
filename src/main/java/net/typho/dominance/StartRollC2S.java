package net.typho.dominance;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record StartRollC2S() implements CustomPayload {
    public static final StartRollC2S INSTANCE = new StartRollC2S();
    public static final PacketCodec<ByteBuf, StartRollC2S> PACKET_CODEC = PacketCodec.unit(INSTANCE);
    public static final Id<StartRollC2S> ID = new Id<>(Dominance.id("start_roll"));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

package net.typho.dominance;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.registry.RegistryWrapper;
import net.typho.dominance.client.DominanceClient;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class DominancePlayerData implements ComponentV3, AutoSyncedComponent, ClientTickingComponent, ServerTickingComponent {
    private int time = 0, cooldown = 0;
    public final PlayerEntity player;

    public DominancePlayerData(PlayerEntity player) {
        this.player = player;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
        Dominance.PLAYER_DATA.sync(player);
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        time = nbt.getInt("time");
        cooldown = nbt.getInt("cooldown");
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("time", time);
        nbt.putInt("cooldown", cooldown);
    }

    @Override
    public void clientTick() {
        if (DominanceClient.ROLL.isPressed() && cooldown == 0 && time == 0 && player.isOnGround()) {
            ((ClientPlayerEntity) player).networkHandler.sendPacket(new CustomPayloadC2SPacket(StartRollC2S.INSTANCE));
        }
    }

    @Override
    public void serverTick() {
        boolean sync = false;

        if (time > 0) {
            time--;

            if (time == 0) {
                cooldown = Dominance.ROLL_COOLDOWN;
            }

            sync = true;

            if (player.isOnGround()) {
                player.addVelocity(player.getRotationVector(0, player.getYaw()).multiply(player.getMovementSpeed() * 20));
                player.velocityModified = true;
            }
        }

        if (cooldown > 0) {
            cooldown--;
            sync = true;
        }

        if (sync) {
            Dominance.PLAYER_DATA.sync(player);
        }
    }
}

package net.typho.dominance;

import net.minecraft.block.FireBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.typho.dominance.client.DominanceClient;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class DominancePlayerData implements ComponentV3, AutoSyncedComponent, ClientTickingComponent, ServerTickingComponent {
    private int time = 0, cooldown = 0;
    private boolean dynamo = false;
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
        Dominance.PLAYER_DATA.sync(player);
    }

    public boolean hasDynamo() {
        return dynamo;
    }

    public void setDynamo(boolean dynamo) {
        this.dynamo = dynamo;
        Dominance.PLAYER_DATA.sync(player);
    }

    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        time = nbt.getInt("time");
        cooldown = nbt.getInt("cooldown");
        dynamo = nbt.getBoolean("dynamo");
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("time", time);
        nbt.putInt("cooldown", cooldown);
        nbt.putBoolean("dynamo", dynamo);
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
                cooldown = (int) player.getAttributeValue(Dominance.PLAYER_ROLL_COOLDOWN);
                dynamo = true;

                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, (int) player.getAttributeValue(Dominance.PLAYER_SWIFT_FOOTED)));
            }

            sync = true;

            if (player.isOnGround()) {
                Vec3d rotVec = player.getRotationVector(0, player.getYaw());

                BlockPos pos = BlockPos.ofFloored(player.getPos().subtract(rotVec.multiply(2)));

                if (player.getRandom().nextDouble() <= player.getAttributeValue(Dominance.PLAYER_FIRE_TRAIL)) {
                    if (FireBlock.canPlaceAt(player.getWorld(), pos, Direction.DOWN)) {
                        player.getWorld().setBlockState(pos, FireBlock.getState(player.getWorld(), player.getBlockPos()));
                    }
                }

                player.addVelocity(rotVec.multiply(player.getMovementSpeed() * 20));
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

package net.typho.dominance.gear;

import net.minecraft.entity.player.PlayerEntity;
import net.typho.dominance.Dominance;

public interface ConsumesSouls {
    default boolean hasEnoughSouls(PlayerEntity player) {
        return Dominance.PLAYER_DATA.get(player).getSouls() > 0;
    }
}

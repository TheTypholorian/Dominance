package net.typho.dominance.mixin;

import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.raid.Raid;
import net.typho.dominance.Dominance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Raid.class)
public abstract class RaidMixin {
    @Shadow
    private int wavesSpawned;

    @Shadow
    public abstract void addRaider(int wave, RaiderEntity raider, @Nullable BlockPos pos, boolean existing);

    @Shadow
    @Final
    private ServerWorld world;

    @Inject(
            method = "spawnNextWave",
            at = @At("TAIL")
    )
    private void create(BlockPos pos, CallbackInfo ci) {
        if (wavesSpawned >= 4) {
            int spawn = wavesSpawned - 2;

            for (int i = 0; i < spawn; i++) {
                addRaider(wavesSpawned, Dominance.ROYAL_GUARD.create(world), pos, false);
            }
        }
    }
}

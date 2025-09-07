package net.typho.dominance;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public class RoyalGuardStatueBlockEntity extends BlockEntity {
    public RoyalGuardStatueBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public RoyalGuardStatueBlockEntity(BlockPos pos, BlockState state) {
        super(Dominance.ROYAL_GUARD_STATUE_BLOCK_ENTITY, pos, state);
    }
}

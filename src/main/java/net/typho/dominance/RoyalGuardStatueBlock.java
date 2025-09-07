package net.typho.dominance;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class RoyalGuardStatueBlock extends BlockWithEntity {
    public static final MapCodec<RoyalGuardStatueBlock> CODEC = createCodec(RoyalGuardStatueBlock::new);
    protected static final VoxelShape SHAPE = Block.createCuboidShape(6, 0, 6, 10, 16, 10);

    protected RoyalGuardStatueBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RoyalGuardStatueBlockEntity(Dominance.ROYAL_GUARD_STATUE_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}

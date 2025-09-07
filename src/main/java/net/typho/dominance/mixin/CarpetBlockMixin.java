package net.typho.dominance.mixin;

import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.typho.dominance.Dominance;
import net.typho.dominance.DominanceBlockProperties;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CarpetBlock.class)
public abstract class CarpetBlockMixin extends Block {
    public CarpetBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return state.get(DominanceBlockProperties.ALTERNATE_CARPET_MODEL) ? BlockRenderType.INVISIBLE : super.getRenderType(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(DominanceBlockProperties.ALTERNATE_CARPET_MODEL);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);

        if (state == null) {
            state = getDefaultState();
        }

        return state.with(DominanceBlockProperties.ALTERNATE_CARPET_MODEL, Dominance.alternateCarpetModel(ctx.getBlockPos(), ctx.getWorld()));
    }

    /**
     * @author The Typhothanian
     * @reason Alternate carpet model
     */
    @Override
    @Overwrite
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
                .with(DominanceBlockProperties.ALTERNATE_CARPET_MODEL, Dominance.alternateCarpetModel(pos, world));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(DominanceBlockProperties.ALTERNATE_CARPET_MODEL)) {
            return VoxelShapes.empty();
        } else {
            return super.getCollisionShape(state, world, pos, context);
        }
    }
}

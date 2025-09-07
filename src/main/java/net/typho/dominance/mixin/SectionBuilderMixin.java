package net.typho.dominance.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.VertexSorter;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadsTransformer;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.typho.dominance.Dominance;
import net.typho.dominance.DominanceBlockProperties;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(SectionBuilder.class)
public abstract class SectionBuilderMixin {
    @Shadow
    @Final
    private BlockRenderManager blockRenderManager;

    @Shadow
    protected abstract BufferBuilder beginBufferBuilding(Map<RenderLayer, BufferBuilder> builders, BlockBufferAllocatorStorage allocatorStorage, RenderLayer layer);

    @Inject(
            method = "build",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;getRenderType()Lnet/minecraft/block/BlockRenderType;"
            )
    )
    private void buildCarpet(ChunkSectionPos sectionPos, ChunkRendererRegion world, VertexSorter vertexSorter, BlockBufferAllocatorStorage allocatorStorage, CallbackInfoReturnable<SectionBuilder.RenderData> cir, @Local BlockState state, @Local MatrixStack matrices, @Local Map<RenderLayer, BufferBuilder> map, @Local(ordinal = 2) BlockPos pos, @Local Random random) {
        if (state.getOrEmpty(DominanceBlockProperties.ALTERNATE_CARPET_MODEL).orElse(false)) {
            BlockState stair = world.getBlockState(pos.down());
            BufferBuilder builder = beginBufferBuilding(map, allocatorStorage, RenderLayers.getBlockLayer(state));
            matrices.push();
            matrices.translate(
                    ChunkSectionPos.getLocalCoord(pos.getX()),
                    ChunkSectionPos.getLocalCoord(pos.getY()),
                    ChunkSectionPos.getLocalCoord(pos.getZ())
            );

            int rot = stair.getOrEmpty(StairsBlock.FACING).map(dir -> switch (dir) {
                case NORTH -> 180;
                case EAST -> 90;
                case WEST -> 270;
                default -> 0;
            }).orElse(0);
            BakedModel original = blockRenderManager.getModel(state);
            BakedModel model = blockRenderManager.getModels().getModelManager().getModel(Dominance.id(switch (stair.getOrEmpty(StairsBlock.SHAPE).orElse(null)) {
                case null:
                case STRAIGHT:
                    yield "block/carpet_side";
                case INNER_RIGHT:
                    rot -= 90;
                case INNER_LEFT:
                    yield "block/carpet_inside";
                case OUTER_LEFT:
                    rot += 90;
                case OUTER_RIGHT:
                    rot += 90;
                    yield "block/carpet_outside";
            }));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot), 0.5f, 0.5f, 0.5f);

            if (model != null) {
                blockRenderManager.getModelRenderer().render(
                        world,
                        new BakedModel() {
                            @Override
                            public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
                                return BakedQuadsTransformer.create().applyingSprite(original.getParticleSprite()).transformAll(model.getQuads(state, face, random));
                            }

                            @Override
                            public boolean useAmbientOcclusion() {
                                return model.useAmbientOcclusion();
                            }

                            @Override
                            public boolean hasDepth() {
                                return model.hasDepth();
                            }

                            @Override
                            public boolean isSideLit() {
                                return model.hasDepth();
                            }

                            @Override
                            public boolean isBuiltin() {
                                return model.hasDepth();
                            }

                            @Override
                            public Sprite getParticleSprite() {
                                return original.getParticleSprite();
                            }

                            @Override
                            public ModelTransformation getTransformation() {
                                return model.getTransformation();
                            }

                            @Override
                            public ModelOverrideList getOverrides() {
                                return model.getOverrides();
                            }
                        },
                        state,
                        pos,
                        matrices,
                        builder,
                        true,
                        random,
                        state.getRenderingSeed(pos),
                        OverlayTexture.DEFAULT_UV
                );
            }

            matrices.pop();
        }
    }
}

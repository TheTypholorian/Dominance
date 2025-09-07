package net.typho.dominance.client;

import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadsTransformer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.List;

public class CarpetBakedModel implements BakedModel {
    public final BakedModel model;
    public final Sprite sprite;
    public final Matrix4f texMat, modelMat;

    public CarpetBakedModel(BakedModel model, Sprite sprite, int rot) {
        this.model = model;
        this.sprite = sprite;

        texMat = new Matrix4f()
                .translate(0.5f, 0.5f, 0)
                .rotateZ((float) Math.toRadians(-rot))
                .translate(-0.5f, -0.5f, 0);

        modelMat = new Matrix4f()
                .rotateAround(RotationAxis.POSITIVE_Y.rotationDegrees(rot), 0, 0, 0);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        BakedQuadsTransformer transformer = BakedQuadsTransformer.create().applyingTransform(modelMat);
        return model.getQuads(state, face, random)
                .stream()
                .map(quad -> {
                    Sprite srcSprite = quad.getSprite();
                    int stride = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeByte() / 4;
                    int[] v = quad.getVertexData();
                    int[] nv = v.clone();

                    for (int i = 0; i < 4; i++) {
                        int offset = i * stride + 4;

                        Vector4f vec = new Vector4f(
                                (Float.intBitsToFloat(v[offset]) - srcSprite.getMinU()) / (srcSprite.getMaxU() - srcSprite.getMinU()),
                                (Float.intBitsToFloat(v[offset + 1]) - srcSprite.getMinV()) / (srcSprite.getMaxV() - srcSprite.getMinV()),
                                0,
                                1
                        );

                        if (quad.getFace().getAxis() == Direction.Axis.Y) {
                            texMat.transform(vec);
                        }

                        nv[offset] = Float.floatToRawIntBits(sprite.getMinU() + vec.x * (sprite.getMaxU() - sprite.getMinU()));
                        nv[offset + 1] = Float.floatToRawIntBits(sprite.getMinV() + vec.y * (sprite.getMaxV() - sprite.getMinV()));
                    }

                    return transformer.transform(new BakedQuad(nv, quad.getColorIndex(), quad.getFace(), sprite, quad.hasShade()));
                })
                .toList();
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
        return model.isSideLit();
    }

    @Override
    public boolean isBuiltin() {
        return model.isBuiltin();
    }

    @Override
    public Sprite getParticleSprite() {
        return sprite;
    }

    @Override
    public ModelTransformation getTransformation() {
        return model.getTransformation();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return model.getOverrides();
    }
}

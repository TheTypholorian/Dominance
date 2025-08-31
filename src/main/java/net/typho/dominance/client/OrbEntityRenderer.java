package net.typho.dominance.client;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EnderDragonEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.typho.dominance.Dominance;
import net.typho.dominance.OrbEntity;
import org.joml.Quaternionf;

import java.util.Objects;

public class OrbEntityRenderer extends EntityRenderer<OrbEntity> {
    protected OrbEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(OrbEntity entity, float yaw, float tickDelta, MatrixStack stack, VertexConsumerProvider vertexConsumers, int light) {
        try {
            RenderLayer layer = VeilRenderType.get(Identifier.of(Dominance.MOD_ID, "orb"));

            if (layer != null) {
                VertexConsumer consumer = vertexConsumers.getBuffer(layer);
                RenderSystem.enableDepthTest();

                stack.push();

                for (int k = 0; k < 1; k++) {
                    if (k == 0) {
                        consumer.vertex(0f, 0f, 1f);
                        consumer.vertex(0f, 1f, 1f);
                        consumer.vertex(1f, 1f, 1f);
                        consumer.vertex(1f, 0f, 1f);
                    }

                    if (k == 1) {
                        consumer.vertex(1f, 0f, 1f);
                        consumer.vertex(1f, 1f, 1f);
                        consumer.vertex(1f, 1f, 0f);
                        consumer.vertex(1f, 0f, 0f);
                    }

                    if (k == 2) {
                        consumer.vertex(1f, 0f, 0f);
                        consumer.vertex(1f, 1f, 0f);
                        consumer.vertex(0f, 1f, 0f);
                        consumer.vertex(0f, 0f, 0f);
                    }

                    if (k == 3) {
                        consumer.vertex(0f, 0f, 0f);
                        consumer.vertex(0f, 1f, 0f);
                        consumer.vertex(0f, 1f, 1f);
                        consumer.vertex(0f, 0f, 1f);
                    }

                    if (k == 4) {
                        consumer.vertex(0f, 0f, 0f);
                        consumer.vertex(0f, 0f, 1f);
                        consumer.vertex(1f, 0f, 1f);
                        consumer.vertex(1f, 0f, 0f);
                    }

                    if (k == 5) {
                        consumer.vertex(0f, 1f, 1f);
                        consumer.vertex(0f, 1f, 0f);
                        consumer.vertex(1f, 1f, 0f);
                        consumer.vertex(1f, 1f, 1f);
                    }
                }

                stack.pop();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        super.render(entity, yaw, tickDelta, stack, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(OrbEntity entity) {
        return null;
    }
}

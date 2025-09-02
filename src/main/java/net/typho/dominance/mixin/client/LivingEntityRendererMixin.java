package net.typho.dominance.mixin.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {
    protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL")
    )
    private void render(T entity, float f, float g, MatrixStack matrices, VertexConsumerProvider output, int i, CallbackInfo ci) {
        double d = dispatcher.getSquaredDistanceToCamera(entity);
        if (!(d > 4096.0)) {
            float hp = entity.getHealth() / entity.getMaxHealth();

            if (hp < 1) {
                matrices.push();
                matrices.translate(0, entity.getBoundingBox().maxY - entity.getBoundingBox().minY + 0.25, 0);
                matrices.multiply(this.dispatcher.getRotation());

                Matrix4f matrix = matrices.peek().getPositionMatrix();
                VertexConsumer consumer = output.getBuffer(RenderLayer.getTextBackground());
                int fullColor = 0xFFFF5555, emptyColor = 0xFF4F1515;
                int light = 0xF000F0;
                float width = 0.5f, height = 0.05f;

                consumer.vertex(matrix, -(width * 2 * (1 - hp) - width), -height, 0.01f)
                        .color(emptyColor)
                        .light(light);
                consumer.vertex(matrix, width, -height, 0.01f)
                        .color(emptyColor)
                        .light(light);
                consumer.vertex(matrix, width, height, 0.01f)
                        .color(emptyColor)
                        .light(light);
                consumer.vertex(matrix, -(width * 2 * (1 - hp) - width), height, 0.01f)
                        .color(emptyColor)
                        .light(light);

                consumer.vertex(matrix, -width, -height, 0.01f)
                        .color(fullColor)
                        .light(light);
                consumer.vertex(matrix, width * 2 * hp - width, -height, 0.01f)
                        .color(fullColor)
                        .light(light);
                consumer.vertex(matrix, width * 2 * hp - width, height, 0.01f)
                        .color(fullColor)
                        .light(light);
                consumer.vertex(matrix, -width, height, 0.01f)
                        .color(fullColor)
                        .light(light);

                matrices.pop();
            }
        }
    }
}

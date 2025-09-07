package net.typho.dominance.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.typho.dominance.Dominance;
import net.typho.dominance.RoyalGuardStatueBlockEntity;
import org.joml.Matrix4fStack;

import java.util.function.BiConsumer;

public class RoyalGuardStatueBlockEntityRenderer implements BlockEntityRenderer<RoyalGuardStatueBlockEntity> {
    public static final Identifier TEXTURE = Dominance.id("textures/entity/illager/royal_guard_statue.png");
    public static final RenderLayer LAYER = RenderLayer.getEntitySolid(TEXTURE);
    public static BiConsumer<VertexConsumer, Integer> MODEL;

    @Override
    public void render(RoyalGuardStatueBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (MODEL != null) {
            Matrix4fStack stack = RenderSystem.getModelViewStack();
            Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

            stack.pushMatrix();
            stack.translate((float) (entity.getPos().getX() - cam.x + 0.5), (float) (entity.getPos().getY() - cam.y), (float) (entity.getPos().getZ() - cam.z + 0.5));

            RenderSystem.applyModelViewMatrix();

            MODEL.accept(vertexConsumers.getBuffer(LAYER), light);

            stack.popMatrix();
        } else {
            System.err.println("Royal guard statue model null during rendering");
        }
    }
}

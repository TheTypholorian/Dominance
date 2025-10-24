package net.typho.dominance.client;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.vertex.VertexArray;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.typho.dominance.Dominance;
import net.typho.dominance.OrbEntity;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

public class OrbEntityRenderer extends EntityRenderer<OrbEntity> {
    public static final VertexArray MODEL = VertexArray.create();

    static {
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        builder.vertex(0f, 0f, 1f).texture(0, 0);
        builder.vertex(0f, 1f, 1f).texture(0, 1);
        builder.vertex(1f, 1f, 1f).texture(1, 1);
        builder.vertex(1f, 0f, 1f).texture(1, 0);

        builder.vertex(1f, 0f, 1f).texture(0, 0);
        builder.vertex(1f, 1f, 1f).texture(0, 1);
        builder.vertex(1f, 1f, 0f).texture(1, 1);
        builder.vertex(1f, 0f, 0f).texture(1, 0);

        builder.vertex(1f, 0f, 0f).texture(0, 0);
        builder.vertex(1f, 1f, 0f).texture(0, 1);
        builder.vertex(0f, 1f, 0f).texture(1, 1);
        builder.vertex(0f, 0f, 0f).texture(1, 0);

        builder.vertex(0f, 0f, 0f).texture(0, 0);
        builder.vertex(0f, 1f, 0f).texture(0, 1);
        builder.vertex(0f, 1f, 1f).texture(1, 1);
        builder.vertex(0f, 0f, 1f).texture(1, 0);

        builder.vertex(0f, 0f, 0f).texture(0, 0);
        builder.vertex(0f, 0f, 1f).texture(0, 1);
        builder.vertex(1f, 0f, 1f).texture(1, 1);
        builder.vertex(1f, 0f, 0f).texture(1, 0);

        builder.vertex(0f, 1f, 1f).texture(0, 0);
        builder.vertex(0f, 1f, 0f).texture(0, 1);
        builder.vertex(1f, 1f, 0f).texture(1, 1);
        builder.vertex(1f, 1f, 1f).texture(1, 0);

        MODEL.upload(builder.end(), VertexArray.DrawUsage.STATIC);
    }

    protected OrbEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(OrbEntity entity, float yaw, float tickDelta, MatrixStack stack, VertexConsumerProvider vertexConsumers, int light) {
        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        RenderLayer layer = VeilRenderType.get(Dominance.id("orb"));

        if (layer != null) {
            Matrix4fStack stack1 = RenderSystem.getModelViewStack();
            stack1.pushMatrix();
            stack1.translate((float) (entity.getPos().getX() - cam.x - 0.5), (float) (entity.getPos().getY() - cam.y + Math.sin(GLFW.glfwGetTime())), (float) (entity.getPos().getZ() - cam.z - 0.5));
            stack1.rotateAround(new Quaternionf().rotationXYZ((float) GLFW.glfwGetTime(), 0, (float) GLFW.glfwGetTime()), 0.5f, 0.5f, 0.5f);
            RenderSystem.applyModelViewMatrix();

            MODEL.bind();
            MODEL.drawWithRenderType(layer);

            stack1.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }

        super.render(entity, yaw, tickDelta, stack, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(OrbEntity entity) {
        return null;
    }
}

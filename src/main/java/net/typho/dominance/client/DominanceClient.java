package net.typho.dominance.client;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.typho.dominance.Dominance;
import net.typho.dominance.OrbEntity;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class DominanceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Dominance.ORB_ENTITY, OrbEntityRenderer::new);
        /*
        WorldRenderEvents.LAST.register(context -> {
            try {
                MinecraftClient client = MinecraftClient.getInstance();

                if (client.player == null) {
                    return;
                }

                RenderLayer layer = VeilRenderType.get(Identifier.of(Dominance.MOD_ID, "orb"));

                if (layer != null) {
                    VertexConsumer consumer = Objects.requireNonNull(context.consumers()).getBuffer(layer);
                    RenderSystem.enableDepthTest();

                    MatrixStack stack = context.matrixStack();
                    stack.push();
                    stack.translate(0, 10, 0);

                    Vec3d cam = context.camera().getPos();
                    Vec3d pos = client.player.getPos();

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
        });
         */
    }
}

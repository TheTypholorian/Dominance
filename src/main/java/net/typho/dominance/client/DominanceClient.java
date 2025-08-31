package net.typho.dominance.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.typho.dominance.Dominance;
import org.lwjgl.glfw.GLFW;

public class DominanceClient implements ClientModInitializer {
    public static final KeyBinding SAVE_LIGHTMAP = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.dominance.debug.save_lightmap",
            GLFW.GLFW_KEY_F9,
            "key.categories.misc"
    ));
    public static final KeyBinding TEST_CUTSCENE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.dominance.debug.test_cutscene",
            GLFW.GLFW_KEY_F8,
            "key.categories.misc"
    ));

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

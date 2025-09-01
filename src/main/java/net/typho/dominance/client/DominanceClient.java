package net.typho.dominance.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.typho.dominance.Dominance;
import net.typho.dominance.RollComponent;
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
    public static final KeyBinding ROLL = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.dominance.roll",
            GLFW.GLFW_KEY_TAB,
            "key.categories.movement"
    ));

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Dominance.ORB_ENTITY, OrbEntityRenderer::new);
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            RollComponent roll = Dominance.ROLL.get(MinecraftClient.getInstance().player);
            int cooldown = (int) ((float) roll.getCooldown() / Dominance.ROLL_COOLDOWN * 17);
            int time = (int) ((float) roll.getTime() / Dominance.ROLL_LENGTH * 17);
            int x = context.getScaledWindowWidth() / 2 + (MinecraftClient.getInstance().player.getMainArm() == Arm.RIGHT ? 115 - 17 : -115), y = context.getScaledWindowHeight() - 17;
            context.drawTexture(Identifier.of("dominance", "textures/gui/roll_full.png"), x, y, 0, 0, 17, 11, 17, 11);

            if (time != 0) {
                context.drawTexture(Identifier.of("dominance", "textures/gui/roll_empty.png"), x, y, 0, 0, 17 - time, 11, 17, 11);
            } else if (cooldown != 0) {
                context.drawTexture(Identifier.of("dominance", "textures/gui/roll_empty.png"), x + 17 - cooldown, y, 17 - cooldown, 0, cooldown, 11, 17, 11);
            }
        });
    }
}

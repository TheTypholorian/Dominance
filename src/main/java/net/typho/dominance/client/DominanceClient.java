package net.typho.dominance.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.typho.dominance.DamageParticleS2C;
import net.typho.dominance.Dominance;
import net.typho.dominance.DominancePlayerData;
import net.typho.dominance.RoyalGuardEntity;
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
    public static final EntityModelLayer ROYAL_GUARD_LAYER = new EntityModelLayer(Dominance.id("royal_guard"), "main");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Dominance.ROYAL_GUARD, RoyalGuardEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(ROYAL_GUARD_LAYER, RoyalGuardEntity::getTexturedModelData);
        ParticleFactoryRegistry.getInstance().register(Dominance.DAMAGE_PARTICLE, DamageParticle::new);
        EntityRendererRegistry.register(Dominance.ORB_ENTITY, OrbEntityRenderer::new);
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (MinecraftClient.getInstance().interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
                PlayerEntity player = MinecraftClient.getInstance().player;
                DominancePlayerData data = Dominance.PLAYER_DATA.get(player);
                int cooldown = (int) ((float) data.getCooldown() / player.getAttributeValue(Dominance.PLAYER_ROLL_COOLDOWN) * 17);
                int time = (int) ((float) data.getTime() / player.getAttributeValue(Dominance.PLAYER_ROLL_LENGTH) * 17);
                int x = context.getScaledWindowWidth() / 2 + (MinecraftClient.getInstance().player.getMainArm() == Arm.RIGHT ? 115 - 17 : -115), y = context.getScaledWindowHeight() - 17;
                context.drawTexture(Identifier.of("dominance", "textures/gui/roll_full.png"), x, y, 0, 0, 17, 11, 17, 11);

                if (time != 0) {
                    context.drawTexture(Identifier.of("dominance", "textures/gui/roll_empty.png"), x, y, 0, 0, 17 - time, 11, 17, 11);
                } else if (cooldown != 0) {
                    context.drawTexture(Identifier.of("dominance", "textures/gui/roll_empty.png"), x + 17 - cooldown, y, 17 - cooldown, 0, cooldown, 11, 17, 11);
                }
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(DamageParticleS2C.ID, (packet, context) -> context.player().getWorld().addParticle(new DamageParticleEffect(Dominance.DAMAGE_PARTICLE, packet.damage()), packet.pos().x, packet.pos().y, packet.pos().z, 0, 0, 0));
        ModelPredicateProviderRegistry.register(Dominance.BURST_CROSSBOW, Identifier.ofVanilla("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return CrossbowItem.isCharged(stack) ? 0.0F : (float)(stack.getMaxUseTime(entity) - entity.getItemUseTimeLeft()) / CrossbowItem.getPullTime(stack, entity);
            }
        });
        ModelPredicateProviderRegistry.register(
                Dominance.BURST_CROSSBOW,
                Identifier.ofVanilla("pulling"),
                (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
        );
        ModelPredicateProviderRegistry.register(Dominance.BURST_CROSSBOW, Identifier.ofVanilla("charged"), (stack, world, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
        ModelPredicateProviderRegistry.register(Dominance.BURST_CROSSBOW, Identifier.ofVanilla("firework"), (stack, world, entity, seed) -> {
            ChargedProjectilesComponent chargedProjectilesComponent = stack.get(DataComponentTypes.CHARGED_PROJECTILES);
            return chargedProjectilesComponent != null && chargedProjectilesComponent.contains(Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
        });
        ModelPredicateProviderRegistry.register(Dominance.HUNTING_BOW, Identifier.ofVanilla("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return entity.getActiveItem() != stack ? 0.0F : (stack.getMaxUseTime(entity) - entity.getItemUseTimeLeft()) / 20.0F;
            }
        });
        ModelPredicateProviderRegistry.register(
                Dominance.HUNTING_BOW,
                Identifier.ofVanilla("pulling"),
                (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F
        );
    }
}

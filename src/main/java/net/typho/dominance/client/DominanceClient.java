package net.typho.dominance.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.typho.dominance.DamageParticleS2C;
import net.typho.dominance.Dominance;
import net.typho.dominance.DominancePlayerData;
import net.typho.dominance.RoyalGuardEntity;
import net.typho.dominance.gear.CorruptedBeaconItem;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

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

    public static BiConsumer<VertexConsumer, Integer> loadObj(ResourceManager manager, Identifier id) {
        return manager.getResource(id).map(resource -> {
            try (InputStream in = resource.getInputStream()) {
                return loadObj(new String(in.readAllBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).orElseThrow();
    }

    public static BiConsumer<VertexConsumer, Integer> loadObj(String input) {
        List<Vector3f> vertices = new LinkedList<>();
        List<Vector2f> texCoords = new LinkedList<>();
        List<Vector3f> normals = new LinkedList<>();

        record Vertex(int v, int tc, int n) {
            public Vertex(String s) {
                this(s.split("/"));
            }

            public Vertex(String[] s) {
                this(Integer.parseInt(s[0]) - 1, Integer.parseInt(s[1]) - 1, Integer.parseInt(s[2]) - 1);
            }

            public void accept(List<Vector3f> vertices, List<Vector2f> texCoords, List<Vector3f> normals, int light, VertexConsumer builder) {
                Vector2f texCoord = texCoords.get(tc);
                Vector3f normal = normals.get(n);
                builder.vertex(vertices.get(v)).color(1f, 1f, 1f, 1f).overlay(0, 0).light(0xF0).texture(texCoord.x, 1 - texCoord.y).normal(normal.x, normal.y, normal.z);
            }
        }

        record Face(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        }

        List<Face> faces = new LinkedList<>();

        for (String line : input.split("\n")) {
            String[] tokens = line.split("\\s+");

            if (tokens.length > 0) {
                switch (tokens[0]) {
                    case "v": {
                        vertices.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
                        break;
                    }
                    case "vt": {
                        texCoords.add(new Vector2f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])));
                        break;
                    }
                    case "vn": {
                        normals.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
                        break;
                    }
                    case "f": {
                        faces.add(new Face(new Vertex(tokens[1]), new Vertex(tokens[2]), new Vertex(tokens[3]), new Vertex(tokens[4])));
                        break;
                    }
                }
            }
        }

        return (builder, light) -> {
            for (Face face : faces) {
                face.v1.accept(vertices, texCoords, normals, light, builder);
                face.v2.accept(vertices, texCoords, normals, light, builder);
                face.v3.accept(vertices, texCoords, normals, light, builder);
                face.v4.accept(vertices, texCoords, normals, light, builder);
            }
        };
    }

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player != null && client.world != null && client.player.getActiveItem().getItem() instanceof CorruptedBeaconItem beacon) {
                float delta = context.tickCounter().getTickDelta(true);
                    beacon.renderBeam(
                            client.player,
                            context.consumers(),
                            context.camera(),
                            delta
                    );
            }
        });
        ModelLoadingPlugin.register(context -> context.addModels(Dominance.id("block/carpet_inside"), Dominance.id("block/carpet_outside"), Dominance.id("block/carpet_side")));
        EntityRendererRegistry.register(Dominance.ORB_ENTITY, OrbEntityRenderer::new);
        EntityRendererRegistry.register(Dominance.ROYAL_GUARD, RoyalGuardEntityRenderer::new);
        EntityRendererRegistry.register(EntityType.VINDICATOR, VindicatorEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(ROYAL_GUARD_LAYER, RoyalGuardEntity::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(EntityModelLayers.VINDICATOR, RoyalGuardEntity::getTexturedModelData);
        ParticleFactoryRegistry.getInstance().register(Dominance.DAMAGE_PARTICLE, DamageParticle::new);
        BlockEntityRendererFactories.register(Dominance.ROYAL_GUARD_STATUE_BLOCK_ENTITY, ctx -> new RoyalGuardStatueBlockEntityRenderer());
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
        ModelPredicateProviderRegistry.register(Dominance.HEAVY_CROSSBOW, Identifier.ofVanilla("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return CrossbowItem.isCharged(stack) ? 0.0F : (float)(stack.getMaxUseTime(entity) - entity.getItemUseTimeLeft()) / CrossbowItem.getPullTime(stack, entity);
            }
        });
        ModelPredicateProviderRegistry.register(
                Dominance.HEAVY_CROSSBOW,
                Identifier.ofVanilla("pulling"),
                (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
        );
        ModelPredicateProviderRegistry.register(Dominance.HEAVY_CROSSBOW, Identifier.ofVanilla("charged"), (stack, world, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
        ModelPredicateProviderRegistry.register(Dominance.HEAVY_CROSSBOW, Identifier.ofVanilla("firework"), (stack, world, entity, seed) -> {
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

package net.typho.dominance;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.model.*;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ShieldItem;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.typho.dominance.gear.RoyalGuardArmorItem;
import net.typho.dominance.gear.RoyalGuardMaceItem;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.minecraft.item.Item.BASE_ATTACK_DAMAGE_MODIFIER_ID;
import static net.minecraft.item.Item.BASE_ATTACK_SPEED_MODIFIER_ID;

public class Dominance implements ModInitializer, EntityComponentInitializer {
    public static final String MOD_ID = "dominance";

    public static <T extends Item> T item(String id, T item) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, id), item);
    }

    public static final Map<Set<ArmorItem>, AttributeModifiersComponent> ARMOR_SET_BONUSES = new LinkedHashMap<>();

    public static final RegistryEntry<ArmorMaterial> ROYAL_GUARD_MATERIAL = Registry.registerReference(Registries.ARMOR_MATERIAL, Identifier.of(MOD_ID, "royal_guard"), new ArmorMaterial(
            Map.of(
                    ArmorItem.Type.BOOTS, 3,
                    ArmorItem.Type.LEGGINGS, 6,
                    ArmorItem.Type.CHESTPLATE, 8,
                    ArmorItem.Type.HELMET, 3,
                    ArmorItem.Type.BODY, 11
            ),
            18,
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            Ingredient::empty,
            List.of(),
            1f,
            0.2f
    ));

    public static final RoyalGuardArmorItem ROYAL_GUARD_HELMET = item("royal_guard_helmet", new RoyalGuardArmorItem(ROYAL_GUARD_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(20)).rarity(Rarity.EPIC)));
    public static final RoyalGuardArmorItem ROYAL_GUARD_CHESTPLATE = item("royal_guard_chestplate", new RoyalGuardArmorItem(ROYAL_GUARD_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings().maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(20)).rarity(Rarity.EPIC)));
    public static final RoyalGuardArmorItem ROYAL_GUARD_BOOTS = item("royal_guard_boots", new RoyalGuardArmorItem(ROYAL_GUARD_MATERIAL, ArmorItem.Type.BOOTS, new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(20)).rarity(Rarity.EPIC)));
    public static final RoyalGuardMaceItem ROYAL_GUARD_MACE = item("royal_guard_mace", new RoyalGuardMaceItem(new Item.Settings().rarity(Rarity.EPIC).attributeModifiers(AttributeModifiersComponent.builder()
            .add(
                    EntityAttributes.GENERIC_ATTACK_DAMAGE,
                    new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 10, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.MAINHAND
            )
            .add(
                    EntityAttributes.GENERIC_ATTACK_SPEED,
                    new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -3.2, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.MAINHAND
            )
            .build())));
    public static final ShieldItem ROYAL_GUARD_SHIELD = item("royal_guard_shield", new ShieldItem(new Item.Settings().rarity(Rarity.EPIC).maxCount(1).maxDamage(504)));

    static {
        ARMOR_SET_BONUSES.put(Set.of(ROYAL_GUARD_HELMET, ROYAL_GUARD_CHESTPLATE, ROYAL_GUARD_BOOTS), AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(Identifier.of(MOD_ID, "royal_guard_set_bonus"), 3, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.ARMOR
                ).build());
    }

    public static final SpriteIdentifier ROYAL_GUARD_SHIELD_SPRITE = new SpriteIdentifier(
            TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.of(MOD_ID, "entity/royal_guard_shield")
    );
    public static final ShieldEntityModel ROYAL_GUARD_SHIELD_MODEL;

    public static final ComponentKey<RollComponent> ROLL = ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of(MOD_ID, "roll"), RollComponent.class);

    static {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("plate", ModelPartBuilder.create().uv(0, 0).cuboid(-7.0F, -11.0F, -2.0F, 14.0F, 22.0F, 1.0F), ModelTransform.NONE);
        modelPartData.addChild("handle", ModelPartBuilder.create().uv(30, 0).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 6.0F), ModelTransform.NONE);
        ROYAL_GUARD_SHIELD_MODEL = new ShieldEntityModel(TexturedModelData.of(modelData, 64, 64).createModel());
    }

    public static final EntityType<OrbEntity> ORB_ENTITY = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "orb"), EntityType.Builder.<OrbEntity>create(OrbEntity::new, SpawnGroup.MISC).dimensions(1f, 1f).build("orb"));

    @Override
    public void onInitialize() {
        ModelPredicateProviderRegistry.register(
                ROYAL_GUARD_SHIELD,
                Identifier.ofVanilla("blocking"),
                (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F
        );
        PayloadTypeRegistry.playC2S().register(StartRollC2S.ID, StartRollC2S.PACKET_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(StartRollC2S.ID, (packet, context) -> {
            RollComponent roll = ROLL.getNullable(context.player());

            if (roll != null && roll.getCooldown() == 0) {
                roll.setTime(20);
            }
        });
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(ROLL, RollComponent::new, RespawnCopyStrategy.NEVER_COPY);
    }
}

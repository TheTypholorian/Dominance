package net.typho.dominance;

import net.fabricmc.api.ModInitializer;
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
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.List;
import java.util.Map;

import static net.minecraft.item.Item.BASE_ATTACK_DAMAGE_MODIFIER_ID;
import static net.minecraft.item.Item.BASE_ATTACK_SPEED_MODIFIER_ID;

public class Dominance implements ModInitializer {
    public static final String MOD_ID = "dominance";

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

    public static final Item ROYAL_GUARD_HELMET = Items.register(Identifier.of(MOD_ID, "royal_guard_helmet"), new RoyalGuardArmorItem(ROYAL_GUARD_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings().rarity(Rarity.EPIC)));
    public static final Item ROYAL_GUARD_CHESTPLATE = Items.register(Identifier.of(MOD_ID, "royal_guard_chestplate"), new RoyalGuardArmorItem(ROYAL_GUARD_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings().rarity(Rarity.EPIC)));
    public static final Item ROYAL_GUARD_BOOTS = Items.register(Identifier.of(MOD_ID, "royal_guard_boots"), new RoyalGuardArmorItem(ROYAL_GUARD_MATERIAL, ArmorItem.Type.BOOTS, new Item.Settings().rarity(Rarity.EPIC)));
    public static final Item ROYAL_GUARD_MACE = Items.register(Identifier.of(MOD_ID, "royal_guard_mace"), new Item(new Item.Settings().rarity(Rarity.EPIC).attributeModifiers(AttributeModifiersComponent.builder()
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
    public static final Item ROYAL_GUARD_SHIELD = Items.register(Identifier.of(MOD_ID, "royal_guard_shield"), new ShieldItem(new Item.Settings().rarity(Rarity.EPIC).maxCount(1).maxDamage(504)));

    public static final SpriteIdentifier ROYAL_GUARD_SHIELD_SPRITE = new SpriteIdentifier(
            TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.of(MOD_ID, "entity/royal_guard_shield")
    );
    public static final ShieldEntityModel ROYAL_GUARD_SHIELD_MODEL;

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
    }
}

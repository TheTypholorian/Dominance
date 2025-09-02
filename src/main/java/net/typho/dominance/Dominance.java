package net.typho.dominance;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.model.*;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.component.ComponentType;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.enchantment.effect.entity.ApplyMobEffectEnchantmentEffect;
import net.minecraft.enchantment.effect.value.MultiplyEnchantmentEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ShieldItem;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleType;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.typho.dominance.client.DamageParticleEffect;
import net.typho.dominance.enchants.*;
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

    public static final int ROLL_LENGTH = 8;
    public static final int ROLL_COOLDOWN = 60;

    public static final SpriteIdentifier ROYAL_GUARD_SHIELD_SPRITE = new SpriteIdentifier(
            TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.of(MOD_ID, "entity/royal_guard_shield")
    );
    public static final ShieldEntityModel ROYAL_GUARD_SHIELD_MODEL;

    public static final ComponentKey<DominancePlayerData> PLAYER_DATA = ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of(MOD_ID, "player_data"), DominancePlayerData.class);

    static {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("plate", ModelPartBuilder.create().uv(0, 0).cuboid(-7.0F, -11.0F, -2.0F, 14.0F, 22.0F, 1.0F), ModelTransform.NONE);
        modelPartData.addChild("handle", ModelPartBuilder.create().uv(30, 0).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 6.0F), ModelTransform.NONE);
        ROYAL_GUARD_SHIELD_MODEL = new ShieldEntityModel(TexturedModelData.of(modelData, 64, 64).createModel());
    }

    public static final ParticleType<DamageParticleEffect> DAMAGE_PARTICLE = Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "damage"), new ParticleType<DamageParticleEffect>(true) {
        @Override
        public MapCodec<DamageParticleEffect> getCodec() {
            return DamageParticleEffect.createCodec(this);
        }

        @Override
        public PacketCodec<? super RegistryByteBuf, DamageParticleEffect> getPacketCodec() {
            return DamageParticleEffect.createPacketCodec(this);
        }
    });

    public static final EntityType<OrbEntity> ORB_ENTITY = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "orb"), EntityType.Builder.<OrbEntity>create(OrbEntity::new, SpawnGroup.MISC).dimensions(1f, 1f).build("orb"));

    public static final RegistryKey<Registry<MapCodec<? extends EnchantmentModifyDamageEffect>>> ENCHANTMENT_DAMAGE_EFFECTS_KEY = RegistryKey.ofRegistry(Identifier.of(MOD_ID, "enchantment_damage_effects"));
    public static final RegistryKey<Registry<MapCodec<? extends EnchantmentPostKillEffect>>> ENCHANTMENT_POST_KILL_EFFECTS_KEY = RegistryKey.ofRegistry(Identifier.of(MOD_ID, "enchantment_post_kill_effects"));
    public static final Registry<MapCodec<? extends EnchantmentModifyDamageEffect>> ENCHANTMENT_DAMAGE_EFFECTS = FabricRegistryBuilder.createSimple(ENCHANTMENT_DAMAGE_EFFECTS_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();
    public static final Registry<MapCodec<? extends EnchantmentPostKillEffect>> ENCHANTMENT_POST_KILL_EFFECTS = FabricRegistryBuilder.createSimple(ENCHANTMENT_POST_KILL_EFFECTS_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentModifyDamageEffect>>> MODIFY_DAMAGE = Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Identifier.of(MOD_ID, "modify_damage"),
            ComponentType.<List<EnchantmentEffectEntry<EnchantmentModifyDamageEffect>>>builder().codec(EnchantmentEffectEntry.createCodec(EnchantmentModifyDamageEffect.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()).build()
    );    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentPostKillEffect>>> POST_KILL = Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Identifier.of(MOD_ID, "post_kill"),
            ComponentType.<List<EnchantmentEffectEntry<EnchantmentPostKillEffect>>>builder().codec(EnchantmentEffectEntry.createCodec(EnchantmentPostKillEffect.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()).build()
    );

    public static final RegistryKey<Enchantment> AMBUSH = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "ambush"));
    public static final RegistryKey<Enchantment> COMMITTED = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "committed"));
    public static final RegistryKey<Enchantment> DYNAMO = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "dynamo"));
    public static final RegistryKey<Enchantment> EXPLODING = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "exploding"));
    public static final RegistryKey<Enchantment> FREEZING = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "freezing"));
    public static final RegistryKey<Enchantment> GRAVITY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "gravity"));
    public static final RegistryKey<Enchantment> BANE_OF_ILLAGERS = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "bane_of_illagers"));
    public static final RegistryKey<Enchantment> LEECHING = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "leeching"));

    public static final MapCodec<AmbushEffect> AMBUSH_EFFECT = Registry.register(ENCHANTMENT_DAMAGE_EFFECTS, Identifier.of(MOD_ID, "ambush"), AmbushEffect.CODEC);
    public static final MapCodec<CommittedEffect> COMMITTED_EFFECT = Registry.register(ENCHANTMENT_DAMAGE_EFFECTS, Identifier.of(MOD_ID, "committed"), CommittedEffect.CODEC);
    public static final MapCodec<DynamoEffect> DYNAMO_EFFECT = Registry.register(ENCHANTMENT_DAMAGE_EFFECTS, Identifier.of(MOD_ID, "dynamo"), DynamoEffect.CODEC);
    public static final MapCodec<ExplodingEffect> EXPLODING_EFFECT = Registry.register(ENCHANTMENT_POST_KILL_EFFECTS, Identifier.of(MOD_ID, "exploding"), ExplodingEffect.CODEC);
    public static final MapCodec<GravityEffect> GRAVITY_EFFECT = Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.of(MOD_ID, "gravity"), GravityEffect.CODEC);
    public static final MapCodec<LeechingEffect> LEECHING_EFFECT = Registry.register(ENCHANTMENT_POST_KILL_EFFECTS, Identifier.of(MOD_ID, "leeching"), LeechingEffect.CODEC);

    @Override
    public void onInitialize() {
        ModelPredicateProviderRegistry.register(
                ROYAL_GUARD_SHIELD,
                Identifier.ofVanilla("blocking"),
                (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F
        );
        PayloadTypeRegistry.playC2S().register(StartRollC2S.ID, StartRollC2S.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DamageParticleS2C.ID, DamageParticleS2C.PACKET_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(StartRollC2S.ID, (packet, context) -> {
            DominancePlayerData data = PLAYER_DATA.getNullable(context.player());

            if (data != null && data.getCooldown() == 0 && data.getTime() == 0) {
                data.setTime(ROLL_LENGTH);
            }
        });
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PLAYER_DATA, DominancePlayerData::new, RespawnCopyStrategy.NEVER_COPY);
    }

    public static void enchantments(Registerable<Enchantment> registerable) {
        RegistryEntryLookup<Item> items = registerable.getRegistryLookup(RegistryKeys.ITEM);
        RegistryEntryLookup<Enchantment> enchantments = registerable.getRegistryLookup(RegistryKeys.ENCHANTMENT);

        registerable.register(AMBUSH, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE),
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                5,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(MODIFY_DAMAGE, new AmbushEffect())
                .build(AMBUSH.getValue()));
        registerable.register(COMMITTED, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE),
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                3,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(MODIFY_DAMAGE, new CommittedEffect())
                .build(COMMITTED.getValue()));
        registerable.register(DYNAMO, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE),
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                3,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(MODIFY_DAMAGE, new DynamoEffect())
                .build(DYNAMO.getValue()));
        registerable.register(EXPLODING, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE),
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                3,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(POST_KILL, new ExplodingEffect())
                .build(EXPLODING.getValue()));
        registerable.register(FREEZING, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE),
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                3,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(EnchantmentEffectComponentTypes.POST_ATTACK, EnchantmentEffectTarget.ATTACKER, EnchantmentEffectTarget.VICTIM, new ApplyMobEffectEnchantmentEffect(
                        RegistryEntryList.of(StatusEffects.SLOWNESS),
                        EnchantmentLevelBasedValue.constant(3),
                        EnchantmentLevelBasedValue.constant(3),
                        EnchantmentLevelBasedValue.linear(1, 1),
                        EnchantmentLevelBasedValue.linear(1, 1)
                ))
                .build(FREEZING.getValue()));
        registerable.register(GRAVITY, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE),
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                3,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(EnchantmentEffectComponentTypes.POST_ATTACK, EnchantmentEffectTarget.ATTACKER, EnchantmentEffectTarget.VICTIM, new GravityEffect())
                .build(GRAVITY.getValue()));
        registerable.register(BANE_OF_ILLAGERS, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE),
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                3,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(
                        EnchantmentEffectComponentTypes.DAMAGE,
                        new MultiplyEnchantmentEffect(EnchantmentLevelBasedValue.linear(1.25f, 0.1f)),
                        EntityPropertiesLootCondition.builder(
                                LootContext.EntityTarget.THIS, EntityPredicate.Builder.create().type(EntityTypePredicate.create(EntityTypeTags.ILLAGER))
                        )
                )
                .exclusiveSet(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE_SET))
                .build(BANE_OF_ILLAGERS.getValue()));
        registerable.register(LEECHING, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE),
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                3,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(POST_KILL, new LeechingEffect())
                .build(LEECHING.getValue()));
    }
}

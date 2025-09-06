package net.typho.dominance;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.model.*;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.component.ComponentType;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.enchantment.effect.entity.ApplyMobEffectEnchantmentEffect;
import net.minecraft.enchantment.effect.value.MultiplyEnchantmentEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.loot.function.SetDamageLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleType;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.typho.dominance.client.DamageParticleEffect;
import net.typho.dominance.enchants.*;
import net.typho.dominance.gear.*;
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

    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }

    public static <T extends Item> T item(String id, T item) {
        return Registry.register(Registries.ITEM, id(id), item);
    }

    public static AttributeModifiersComponent weaponAttributes(double damage, double speed) {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, damage, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, speed, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    public static final TagKey<ArmorMaterial> PIGLIN_NEUTRAL = TagKey.of(RegistryKeys.ARMOR_MATERIAL, id("piglin_neutral"));
    public static final TagKey<Item> ANY_REFORGABLE = TagKey.of(RegistryKeys.ITEM, id("reforgable/any"));
    public static final TagKey<Item> ARMOR_REFORGABLE = TagKey.of(RegistryKeys.ITEM, id("reforgable/armor"));
    public static final TagKey<Item> MELEE_REFORGABLE = TagKey.of(RegistryKeys.ITEM, id("reforgable/melee"));
    public static final TagKey<Item> CROSSBOWS = TagKey.of(RegistryKeys.ITEM, id("crossbows"));

    public static final RecipeSerializer<ReforgeRecipe> REFORGE_RECIPE = Registry.register(Registries.RECIPE_SERIALIZER, id("reforge"), new ReforgeRecipe.Serializer());

    public static final RegistryKey<Registry<Reforge.Factory<?>>> REFORGE_KEY = RegistryKey.ofRegistry(id("reforge"));
    public static final ComponentType<Reforge> REFORGE_COMPONENT = Registry.register(Registries.DATA_COMPONENT_TYPE, id("reforge"), new ComponentType.Builder<Reforge>().codec(Reforge.CODEC).packetCodec(Reforge.PACKET_CODEC).build());

    public static final RegistryKey<Registry<Reforge.Type<?>>> REFORGE_TYPE_KEY = RegistryKey.ofRegistry(id("reforge_type"));
    public static final Registry<Reforge.Type<?>> REFORGE_TYPE = FabricRegistryBuilder.createSimple(REFORGE_TYPE_KEY).buildAndRegister();

    public static final RegistryEntry<Reforge.Type<?>> ATTRIBUTES_REFORGE_TYPE = Registry.registerReference(REFORGE_TYPE, id("attribute"), (Reforge.Type<BasicReforge.Factory>) () -> BasicReforge.Factory.CODEC);

    public static final Map<Set<ArmorItem>, AttributeModifiersComponent> ARMOR_SET_BONUSES = new LinkedHashMap<>();

    public static final RegistryEntry<ArmorMaterial> ROYAL_GUARD_MATERIAL = Registry.registerReference(Registries.ARMOR_MATERIAL, id("royal_guard"), new ArmorMaterial(
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
            3f,
            0.2f
    ));
    public static final RegistryEntry<ArmorMaterial> EVOCATION_MATERIAL = Registry.registerReference(Registries.ARMOR_MATERIAL, id("evocation"), new ArmorMaterial(
            Map.of(
                    ArmorItem.Type.BOOTS, 1,
                    ArmorItem.Type.LEGGINGS, 2,
                    ArmorItem.Type.CHESTPLATE, 3,
                    ArmorItem.Type.HELMET, 1,
                    ArmorItem.Type.BODY, 3
            ),
            20,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
            Ingredient::empty,
            List.of(),
            0,
            0
    ));
    public static final RegistryEntry<ArmorMaterial> PIGLIN_MATERIAL = Registry.registerReference(Registries.ARMOR_MATERIAL, id("piglin"), new ArmorMaterial(
            Map.of(
                    ArmorItem.Type.BOOTS, 3,
                    ArmorItem.Type.LEGGINGS, 6,
                    ArmorItem.Type.CHESTPLATE, 8,
                    ArmorItem.Type.HELMET, 3,
                    ArmorItem.Type.BODY, 11
            ),
            15,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
            () -> Ingredient.ofItems(Items.LEATHER),
            List.of(),
            1f,
            0.1f
    ));

    public static final RoyalGuardArmorItem ROYAL_GUARD_HELMET = item("royal_guard_helmet", new RoyalGuardArmorItem(ROYAL_GUARD_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings().maxCount(1).maxDamage(ArmorItem.Type.HELMET.getMaxDamage(20)).rarity(Rarity.RARE)));
    public static final RoyalGuardArmorItem ROYAL_GUARD_CHESTPLATE = item("royal_guard_chestplate", new RoyalGuardArmorItem(ROYAL_GUARD_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings().maxCount(1).maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(20)).rarity(Rarity.RARE)));
    public static final RoyalGuardArmorItem ROYAL_GUARD_BOOTS = item("royal_guard_boots", new RoyalGuardArmorItem(ROYAL_GUARD_MATERIAL, ArmorItem.Type.BOOTS, new Item.Settings().maxCount(1).maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(20)).rarity(Rarity.RARE)));
    public static final SplashWeaponItem ROYAL_GUARD_MACE = item("royal_guard_mace", new SplashWeaponItem(1, 7, 0.5f, new Item.Settings().maxCount(1).maxDamage(500).rarity(Rarity.RARE).attributeModifiers(weaponAttributes(10, -3.2))));
    public static final ShieldItem ROYAL_GUARD_SHIELD = item("royal_guard_shield", new ShieldItem(new Item.Settings().rarity(Rarity.RARE).maxCount(1).maxDamage(504)));
    public static final EvocationRobeItem EVOCATION_HAT = item("evocation_hat", new EvocationRobeItem(EVOCATION_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings().maxCount(1).maxDamage(ArmorItem.Type.HELMET.getMaxDamage(22)).rarity(Rarity.EPIC)));
    public static final EvocationRobeItem EVOCATION_ROBE = item("evocation_robe", new EvocationRobeItem(EVOCATION_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings().maxCount(1).maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(22)).rarity(Rarity.EPIC)));
    public static final PiglinArmorItem PIGLIN_HELMET = item("piglin_helmet", new PiglinArmorItem(PIGLIN_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings().maxCount(1).maxDamage(ArmorItem.Type.HELMET.getMaxDamage(15)).rarity(Rarity.UNCOMMON)));
    public static final PiglinArmorItem PIGLIN_CHESTPLATE = item("piglin_chestplate", new PiglinArmorItem(PIGLIN_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings().maxCount(1).maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(15)).rarity(Rarity.UNCOMMON)));
    public static final SplashWeaponItem GREAT_HAMMER = item("great_hammer", new SplashWeaponItem(3, 5, 1, new Item.Settings().maxCount(1).maxDamage(500).rarity(Rarity.RARE).attributeModifiers(weaponAttributes(14, -3))));
    public static final SplashWeaponItem BASTION_BUSTER = item("bastion_buster", new SplashWeaponItem(4, 6, 1, new Item.Settings().maxCount(1).maxDamage(600).rarity(Rarity.EPIC).attributeModifiers(weaponAttributes(15, -2.8))));
    public static final SwordItem KATANA = item("katana", new SwordItem(ToolMaterials.IRON, new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON).attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 4, -2f))));
    public static final BurstCrossbowItem BURST_CROSSBOW = item("burst_crossbow", new BurstCrossbowItem(new Item.Settings().rarity(Rarity.RARE).maxCount(1).maxDamage(465)));
    public static final HeavyCrossbowItem HEAVY_CROSSBOW = item("heavy_crossbow", new HeavyCrossbowItem(new Item.Settings().rarity(Rarity.UNCOMMON).maxCount(1).maxDamage(465)));
    public static final HuntingBowItem HUNTING_BOW = item("hunting_bow", new HuntingBowItem(new Item.Settings().rarity(Rarity.UNCOMMON).maxCount(1).maxDamage(384)));

    static {
        ARMOR_SET_BONUSES.put(Set.of(ROYAL_GUARD_HELMET, ROYAL_GUARD_CHESTPLATE, ROYAL_GUARD_BOOTS), AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(id("royal_guard_set_bonus"), 3, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.ARMOR
                ).build());
        ARMOR_SET_BONUSES.put(Set.of(EVOCATION_HAT, EVOCATION_ROBE), AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        new EntityAttributeModifier(id("evocation_robe_set_bonus"), 0.25, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        AttributeModifierSlot.ARMOR
                ).build());
    }

    public static final SpriteIdentifier ROYAL_GUARD_SHIELD_SPRITE = new SpriteIdentifier(
            TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, id("entity/royal_guard_shield")
    );
    public static final ShieldEntityModel ROYAL_GUARD_SHIELD_MODEL;

    public static final EntityType<RoyalGuardEntity> ROYAL_GUARD = Registry.register(Registries.ENTITY_TYPE, id("royal_guard"), EntityType.Builder.create(RoyalGuardEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.95F)
            .passengerAttachments(2.0F)
            .vehicleAttachment(-0.6F)
            .maxTrackingRange(8)
            .build("royal_guard"));
    public static final SpawnEggItem ROYAL_GUARD_SPAWN_EGG = item("royal_guard_spawn_egg", new SpawnEggItem(ROYAL_GUARD, 0x3C3C3C, 0xFFC444, new Item.Settings()));

    public static final ComponentKey<DominancePlayerData> PLAYER_DATA = ComponentRegistryV3.INSTANCE.getOrCreate(id("player_data"), DominancePlayerData.class);

    static {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("plate", ModelPartBuilder.create().uv(0, 0).cuboid(-7.0F, -11.0F, -2.0F, 14.0F, 22.0F, 1.0F), ModelTransform.NONE);
        modelPartData.addChild("handle", ModelPartBuilder.create().uv(30, 0).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 6.0F), ModelTransform.NONE);
        ROYAL_GUARD_SHIELD_MODEL = new ShieldEntityModel(TexturedModelData.of(modelData, 64, 64).createModel());
    }

    public static final RegistryEntry<StatusEffect> RAMPAGE_STATUS_EFFECT = Registry.registerReference(Registries.STATUS_EFFECT, id("rampage"), new RampageStatusEffect(StatusEffectCategory.BENEFICIAL, 0x660000));

    public static final ParticleType<DamageParticleEffect> DAMAGE_PARTICLE = Registry.register(Registries.PARTICLE_TYPE, id("damage"), new ParticleType<DamageParticleEffect>(true) {
        @Override
        public MapCodec<DamageParticleEffect> getCodec() {
            return DamageParticleEffect.createCodec(this);
        }

        @Override
        public PacketCodec<? super RegistryByteBuf, DamageParticleEffect> getPacketCodec() {
            return DamageParticleEffect.createPacketCodec(this);
        }
    });

    public static final EntityType<OrbEntity> ORB_ENTITY = Registry.register(Registries.ENTITY_TYPE, id("orb"), EntityType.Builder.<OrbEntity>create(OrbEntity::new, SpawnGroup.MISC).dimensions(1f, 1f).build("orb"));

    public static final RegistryKey<Registry<MapCodec<? extends EnchantmentModifyDamageEffect>>> ENCHANTMENT_DAMAGE_EFFECTS_KEY = RegistryKey.ofRegistry(id("enchantment_damage_effects"));
    public static final RegistryKey<Registry<MapCodec<? extends EnchantmentPostKillEffect>>> ENCHANTMENT_POST_KILL_EFFECTS_KEY = RegistryKey.ofRegistry(id("enchantment_post_kill_effects"));
    public static final Registry<MapCodec<? extends EnchantmentModifyDamageEffect>> ENCHANTMENT_DAMAGE_EFFECTS = FabricRegistryBuilder.createSimple(ENCHANTMENT_DAMAGE_EFFECTS_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();
    public static final Registry<MapCodec<? extends EnchantmentPostKillEffect>> ENCHANTMENT_POST_KILL_EFFECTS = FabricRegistryBuilder.createSimple(ENCHANTMENT_POST_KILL_EFFECTS_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentModifyDamageEffect>>> MODIFY_DAMAGE = Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, id("modify_damage"),
            ComponentType.<List<EnchantmentEffectEntry<EnchantmentModifyDamageEffect>>>builder().codec(EnchantmentEffectEntry.createCodec(EnchantmentModifyDamageEffect.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()).build()
    );    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentPostKillEffect>>> POST_KILL = Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, id("post_kill"),
            ComponentType.<List<EnchantmentEffectEntry<EnchantmentPostKillEffect>>>builder().codec(EnchantmentEffectEntry.createCodec(EnchantmentPostKillEffect.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()).build()
    );

    public static final RegistryEntry<EntityAttribute> PLAYER_ROLL_COOLDOWN = Registry.registerReference(Registries.ATTRIBUTE, id("generic.player.roll_cooldown"), new ClampedEntityAttribute("attribute.name.dominance.player.roll_cooldown", 60, 0, 100));
    public static final RegistryEntry<EntityAttribute> PLAYER_ROLL_LENGTH = Registry.registerReference(Registries.ATTRIBUTE, id("generic.player.roll_length"), new ClampedEntityAttribute("attribute.name.dominance.player.roll_length", 8, 0, 40));
    public static final RegistryEntry<EntityAttribute> PLAYER_FIRE_TRAIL = Registry.registerReference(Registries.ATTRIBUTE, id("generic.player.fire_trail"), new ClampedEntityAttribute("attribute.name.dominance.player.fire_trail", 0, 0, 1));
    public static final RegistryEntry<EntityAttribute> PLAYER_SWIFT_FOOTED = Registry.registerReference(Registries.ATTRIBUTE, id("generic.player.swift_footed"), new ClampedEntityAttribute("attribute.name.dominance.player.swift_footed", 0, 0, 1200));

    public static final TagKey<Enchantment> INFLICT_EXCLUSIVE_SET = TagKey.of(RegistryKeys.ENCHANTMENT, id("exclusive_set/inflict"));
    public static final TagKey<Enchantment> CONDITIONAL_EXCLUSIVE_SET = TagKey.of(RegistryKeys.ENCHANTMENT, id("exclusive_set/conditional"));
    public static final TagKey<Enchantment> ROLL_EXCLUSIVE_SET = TagKey.of(RegistryKeys.ENCHANTMENT, id("exclusive_set/roll"));

    public static final RegistryKey<Enchantment> AMBUSH = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("ambush"));
    public static final RegistryKey<Enchantment> COMMITTED = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("committed"));
    public static final RegistryKey<Enchantment> COWARDICE = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("cowardice"));
    public static final RegistryKey<Enchantment> DYNAMO = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("dynamo"));
    public static final RegistryKey<Enchantment> EXPLODING = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("exploding"));
    public static final RegistryKey<Enchantment> FREEZING = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("freezing"));
    public static final RegistryKey<Enchantment> GRAVITY = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("gravity"));
    public static final RegistryKey<Enchantment> BANE_OF_ILLAGERS = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("bane_of_illagers"));
    public static final RegistryKey<Enchantment> LEECHING = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("leeching"));
    public static final RegistryKey<Enchantment> RAMPAGE = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("rampage"));
    public static final RegistryKey<Enchantment> WEAKENING = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("weakening"));

    public static final RegistryKey<Enchantment> ACROBAT = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("acrobat"));
    public static final RegistryKey<Enchantment> FIRE_TRAIL = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("fire_trail"));
    public static final RegistryKey<Enchantment> RECKLESS = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("reckless"));
    public static final RegistryKey<Enchantment> SWIFT_FOOTED = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("swift_footed"));

    public static final RegistryKey<Reforge.Factory<?>> LIGHTWEIGHT = RegistryKey.of(REFORGE_KEY, id("lightweight"));
    public static final RegistryKey<Reforge.Factory<?>> HEAVY_MELEE = RegistryKey.of(REFORGE_KEY, id("heavy_melee"));
    public static final RegistryKey<Reforge.Factory<?>> LENGTHY = RegistryKey.of(REFORGE_KEY, id("lengthy"));
    public static final RegistryKey<Reforge.Factory<?>> SHARP = RegistryKey.of(REFORGE_KEY, id("sharp"));
    public static final RegistryKey<Reforge.Factory<?>> WEAK = RegistryKey.of(REFORGE_KEY, id("weak"));
    public static final RegistryKey<Reforge.Factory<?>> SLOW = RegistryKey.of(REFORGE_KEY, id("slow"));

    public static final RegistryKey<Reforge.Factory<?>> FORTIFIED = RegistryKey.of(REFORGE_KEY, id("fortified"));
    public static final RegistryKey<Reforge.Factory<?>> NETHER_FORGED = RegistryKey.of(REFORGE_KEY, id("nether_forged"));
    public static final RegistryKey<Reforge.Factory<?>> SWIFT = RegistryKey.of(REFORGE_KEY, id("swift"));
    public static final RegistryKey<Reforge.Factory<?>> VITALITY = RegistryKey.of(REFORGE_KEY, id("vitality"));
    public static final RegistryKey<Reforge.Factory<?>> CRUMBLING = RegistryKey.of(REFORGE_KEY, id("crumbling"));
    public static final RegistryKey<Reforge.Factory<?>> HEAVY_ARMOR = RegistryKey.of(REFORGE_KEY, id("heavy_armor"));

    public static final MapCodec<AmbushEffect> AMBUSH_EFFECT = Registry.register(ENCHANTMENT_DAMAGE_EFFECTS, id("ambush"), AmbushEffect.CODEC);
    public static final MapCodec<CommittedEffect> COMMITTED_EFFECT = Registry.register(ENCHANTMENT_DAMAGE_EFFECTS, id("committed"), CommittedEffect.CODEC);
    public static final MapCodec<CowardiceEffect> COWARDICE_EFFECT = Registry.register(ENCHANTMENT_DAMAGE_EFFECTS, id("cowardice"), CowardiceEffect.CODEC);
    public static final MapCodec<DynamoEffect> DYNAMO_EFFECT = Registry.register(ENCHANTMENT_DAMAGE_EFFECTS, id("dynamo"), DynamoEffect.CODEC);
    public static final MapCodec<ExplodingEffect> EXPLODING_EFFECT = Registry.register(ENCHANTMENT_POST_KILL_EFFECTS, id("exploding"), ExplodingEffect.CODEC);
    public static final MapCodec<GravityEffect> GRAVITY_EFFECT = Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, id("gravity"), GravityEffect.CODEC);
    public static final MapCodec<LeechingEffect> LEECHING_EFFECT = Registry.register(ENCHANTMENT_POST_KILL_EFFECTS, id("leeching"), LeechingEffect.CODEC);
    public static final MapCodec<RampageEffect> RAMPAGE_EFFECT = Registry.register(ENCHANTMENT_POST_KILL_EFFECTS, id("rampage"), RampageEffect.CODEC);
    public static final MapCodec<WeakeningEffect> WEAKENING_EFFECT = Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, id("weakening"), WeakeningEffect.CODEC);

    public static final SmithingTemplateItem REFORGE_SMITHING_TEMPLATE = item("reforge_smithing_template", new SmithingTemplateItem(
            Text.translatable("item.dominance.reforge_smithing_template.applies_to").formatted(Formatting.BLUE),
            Text.translatable("item.dominance.reforge_smithing_template.ingredients").formatted(Formatting.BLUE),
            Text.translatable("upgrade.dominance.reforge").formatted(Formatting.BLUE),
            Text.translatable("item.dominance.reforge_smithing_template.base_slot_description"),
            Text.translatable("item.dominance.reforge_smithing_template.additions_slot_description"),
            SmithingTemplateItem.getNetheriteUpgradeEmptyBaseSlotTextures(),
            SmithingTemplateItem.getNetheriteUpgradeEmptyAdditionsSlotTextures()
    ));

    public static final RegistryKey<ItemGroup> CREATIVE_TAB_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, id("tab"));
    public static final ItemGroup CREATIVE_TAB = Registry.register(Registries.ITEM_GROUP, CREATIVE_TAB_KEY, FabricItemGroup.builder()
            .icon(() -> new ItemStack(GREAT_HAMMER))
            .displayName(Text.translatable("itemGroup.dominance"))
            .build());

    @Override
    @SuppressWarnings("unchecked")
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(ROYAL_GUARD, RoyalGuardEntity.createRoyalGuardAttributes());
        DynamicRegistries.registerSynced(REFORGE_KEY, Reforge.Factory.CODEC);
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
                data.setTime((int) context.player().getAttributeValue(PLAYER_ROLL_LENGTH));
            }
        });
        ItemGroupEvents.modifyEntriesEvent(CREATIVE_TAB_KEY).register(entries -> {
            entries.add(ROYAL_GUARD_HELMET);
            entries.add(ROYAL_GUARD_CHESTPLATE);
            entries.add(ROYAL_GUARD_BOOTS);
            entries.add(ROYAL_GUARD_SHIELD);
            entries.add(EVOCATION_HAT);
            entries.add(EVOCATION_ROBE);
            entries.add(PIGLIN_HELMET);
            entries.add(PIGLIN_CHESTPLATE);
            entries.add(ROYAL_GUARD_MACE);
            entries.add(GREAT_HAMMER);
            entries.add(BASTION_BUSTER);
            entries.add(KATANA);
            entries.add(BURST_CROSSBOW);
            entries.add(HEAVY_CROSSBOW);
            entries.add(HUNTING_BOW);
            entries.add(REFORGE_SMITHING_TEMPLATE);
            entries.add(ROYAL_GUARD_SPAWN_EGG);
        });
        LootTableEvents.MODIFY.register((key, builder, source, wrapperLookup) -> {
            if (key == LootTables.BASTION_TREASURE_CHEST) {
                builder.pool(
                        LootPool.builder()
                                .with(ItemEntry.builder(PIGLIN_HELMET).weight(5)
                                        .apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.8f, 1)))
                                        .apply(EnchantRandomlyLootFunction.builder(wrapperLookup)))
                                .with(ItemEntry.builder(PIGLIN_CHESTPLATE).weight(5)
                                        .apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.8f, 1)))
                                        .apply(EnchantRandomlyLootFunction.builder(wrapperLookup)))
                                .with(ItemEntry.builder(BASTION_BUSTER).weight(2)
                                        .apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.8f, 1)))
                                        .apply(EnchantRandomlyLootFunction.builder(wrapperLookup)))
                );
            } else if (key.getValue().getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && key.getValue().getPath().equals("entities/evoker")) {
                builder.pool(
                        LootPool.builder()
                                .with(ItemEntry.builder(EVOCATION_HAT).weight(5))
                                .with(ItemEntry.builder(EVOCATION_ROBE).weight(5))
                );
            }
        });
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandDispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("reforge")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> {
                        if (context.getSource().isExecutedByPlayer()) {
                            ItemStack stack = context.getSource().getPlayerOrThrow().getMainHandStack();
                            Reforge.Factory<?> reforge = Reforge.pickForStack(stack, context.getSource().getWorld().getRegistryManager());

                            if (reforge == null) {
                                context.getSource().sendFeedback(() -> Text.translatable("command.dominance.reforge.no_possible_reforge").formatted(Formatting.RED), false);
                                return 0;
                            }

                            stack.set(REFORGE_COMPONENT, reforge.generate(stack));
                            return 1;
                        }

                        return 0;
                    })
                    .then(CommandManager.argument("type", RegistryKeyArgumentType.registryKey(REFORGE_KEY))
                            .executes(context -> {
                                if (context.getSource().isExecutedByPlayer()) {
                                    ItemStack stack = context.getSource().getPlayerOrThrow().getMainHandStack();
                                    RegistryKey<Reforge.Factory<?>> key = (RegistryKey<Reforge.Factory<?>>) context.getArgument("type", RegistryKey.class);
                                    Reforge.Factory<?> reforge = context.getSource().getWorld().getRegistryManager().get(REFORGE_KEY).get(key);

                                    if (reforge == null) {
                                        context.getSource().sendFeedback(() -> Text.translatable("command.dominance.reforge.no_reforge", key.getValue().toString()).formatted(Formatting.RED), false);
                                        return 0;
                                    }

                                    if (!reforge.isValidItem(stack)) {
                                        context.getSource().sendFeedback(() -> Text.translatable("command.dominance.reforge.bad_item", key.getValue().toString(), stack.getItem().getName()).formatted(Formatting.RED), false);
                                        return 0;
                                    }

                                    stack.set(REFORGE_COMPONENT, reforge.generate(stack));
                                    return 1;
                                }

                                return 0;
                            })
                            .then(CommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound())
                                    .executes(context -> {
                                        if (context.getSource().isExecutedByPlayer()) {
                                            ItemStack stack = context.getSource().getPlayerOrThrow().getMainHandStack();
                                            RegistryKey<Reforge.Factory<?>> key = (RegistryKey<Reforge.Factory<?>>) context.getArgument("type", RegistryKey.class);
                                            Reforge.Factory<?> reforge = context.getSource().getWorld().getRegistryManager().get(REFORGE_KEY).get(key);

                                            if (reforge == null) {
                                                context.getSource().sendFeedback(() -> Text.translatable("command.dominance.reforge.no_reforge", key.getValue().toString()).formatted(Formatting.RED), false);
                                                return 0;
                                            }

                                            if (!reforge.isValidItem(stack)) {
                                                context.getSource().sendFeedback(() -> Text.translatable("command.dominance.reforge.bad_item", key.getValue().toString(), stack.getItem().getName()).formatted(Formatting.RED), false);
                                                return 0;
                                            }

                                            NbtCompound nbt = context.getArgument("nbt", NbtCompound.class);
                                            DataResult<? extends Pair<? extends Reforge, NbtElement>> result = reforge.codec().codec().decode(NbtOps.INSTANCE, nbt);

                                            if (result.isError()) {
                                                context.getSource().sendFeedback(() -> Text.translatable("command.dominance.reforge.bad_nbt", nbt).formatted(Formatting.RED), false);
                                                return 0;
                                            }

                                            stack.set(REFORGE_COMPONENT, result.getOrThrow().getFirst());
                                            return 1;
                                        }

                                        return 0;
                                    }))));
        });
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PLAYER_DATA, DominancePlayerData::new, RespawnCopyStrategy.NEVER_COPY);
    }

    public static void reforges(Registerable<Reforge.Factory<?>> registerable) {
        registerable.register(LIGHTWEIGHT, new BasicReforge.Factory(
                LIGHTWEIGHT,
                Rarity.COMMON,
                List.of(new BasicReforge.Entry(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        0.1,
                        0.2,
                        0.05,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ), new BasicReforge.Entry(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        -0.5,
                        -3,
                        0.5,
                        EntityAttributeModifier.Operation.ADD_VALUE
                )),
                MELEE_REFORGABLE
        ));
        registerable.register(HEAVY_MELEE, new BasicReforge.Factory(
                HEAVY_MELEE,
                Rarity.COMMON,
                List.of(new BasicReforge.Entry(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        -0.1,
                        -0.2,
                        0.05,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ), new BasicReforge.Entry(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        0.5,
                        3,
                        0.5,
                        EntityAttributeModifier.Operation.ADD_VALUE
                )),
                MELEE_REFORGABLE
        ));
        registerable.register(LENGTHY, new BasicReforge.Factory(
                LENGTHY,
                Rarity.UNCOMMON,
                List.of(new BasicReforge.Entry(
                        EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
                        0.25,
                        2,
                        0.25,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ), new BasicReforge.Entry(
                        EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE,
                        0.25,
                        2,
                        0.25,
                        EntityAttributeModifier.Operation.ADD_VALUE
                )),
                MELEE_REFORGABLE
        ));
        registerable.register(SHARP, new BasicReforge.Factory(
                WEAK,
                Rarity.COMMON,
                List.of(new BasicReforge.Entry(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        0.5,
                        3,
                        0.5,
                        EntityAttributeModifier.Operation.ADD_VALUE
                )),
                MELEE_REFORGABLE
        ));
        registerable.register(WEAK, new BasicReforge.Factory(
                WEAK,
                Rarity.COMMON,
                List.of(new BasicReforge.Entry(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        -0.5,
                        -3,
                        0.5,
                        EntityAttributeModifier.Operation.ADD_VALUE
                )),
                MELEE_REFORGABLE
        ));
        registerable.register(SLOW, new BasicReforge.Factory(
                SLOW,
                Rarity.COMMON,
                List.of(new BasicReforge.Entry(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        -0.1,
                        -0.25,
                        0.05,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )),
                MELEE_REFORGABLE
        ));

        registerable.register(FORTIFIED, new BasicReforge.Factory(
                FORTIFIED,
                Rarity.UNCOMMON,
                List.of(new BasicReforge.Entry(
                        EntityAttributes.GENERIC_ARMOR,
                        0.25,
                        1,
                        0.1,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ), new BasicReforge.Entry(
                        EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                        0.1,
                        0.25,
                        0.05,
                        EntityAttributeModifier.Operation.ADD_VALUE
                )),
                ARMOR_REFORGABLE
        ));
        registerable.register(NETHER_FORGED, new BasicReforge.Factory(
                NETHER_FORGED,
                Rarity.RARE,
                List.of(new BasicReforge.Entry(
                        EntityAttributes.GENERIC_BURNING_TIME,
                        -0.1,
                        -0.25,
                        0.05,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ), new BasicReforge.Entry(
                        EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                        0.25,
                        2,
                        0.25,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )),
                ARMOR_REFORGABLE
        ));
        registerable.register(SWIFT, new BasicReforge.Factory(
                SWIFT,
                Rarity.UNCOMMON,
                List.of(new BasicReforge.Entry(
                        EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        0.1,
                        0.25,
                        0.05,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ), new BasicReforge.Entry(
                        EntityAttributes.PLAYER_SNEAKING_SPEED,
                        0.1,
                        0.5,
                        0.1,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )),
                ARMOR_REFORGABLE
        ));
        registerable.register(VITALITY, new BasicReforge.Factory(
                VITALITY,
                Rarity.EPIC,
                List.of(new BasicReforge.Entry(
                        EntityAttributes.GENERIC_MAX_HEALTH,
                        0.5,
                        2,
                        0.5,
                        EntityAttributeModifier.Operation.ADD_VALUE
                )),
                ARMOR_REFORGABLE
        ));
        registerable.register(CRUMBLING, new BasicReforge.Factory(
                CRUMBLING,
                Rarity.COMMON,
                List.of(new BasicReforge.Entry(
                        EntityAttributes.GENERIC_ARMOR,
                        -0.25,
                        -2,
                        0.25,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ), new BasicReforge.Entry(
                        EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                        -0.1,
                        -0.25,
                        0.05,
                        EntityAttributeModifier.Operation.ADD_VALUE
                )),
                ARMOR_REFORGABLE
        ));
        registerable.register(HEAVY_ARMOR, new BasicReforge.Factory(
                HEAVY_ARMOR,
                Rarity.COMMON,
                List.of(new BasicReforge.Entry(
                        EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        -0.1,
                        -0.25,
                        0.05,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ), new BasicReforge.Entry(
                        EntityAttributes.PLAYER_SNEAKING_SPEED,
                        -0.1,
                        -0.5,
                        0.1,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )),
                ARMOR_REFORGABLE
        ));
    }

    public static void enchantments(Registerable<Enchantment> registerable) {
        RegistryEntryLookup<Item> items = registerable.getRegistryLookup(RegistryKeys.ITEM);
        RegistryEntryLookup<Enchantment> enchantments = registerable.getRegistryLookup(RegistryKeys.ENCHANTMENT);

        registerable.register(AMBUSH, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                6,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(MODIFY_DAMAGE, new AmbushEffect())
                .exclusiveSet(enchantments.getOrThrow(CONDITIONAL_EXCLUSIVE_SET))
                .build(AMBUSH.getValue()));
        registerable.register(COMMITTED, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                2,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(MODIFY_DAMAGE, new CommittedEffect())
                .exclusiveSet(enchantments.getOrThrow(CONDITIONAL_EXCLUSIVE_SET))
                .build(COMMITTED.getValue()));
        registerable.register(COWARDICE, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                4,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(MODIFY_DAMAGE, new CowardiceEffect())
                .build(COWARDICE.getValue()));
        registerable.register(DYNAMO, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                4,
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
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                2,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(POST_KILL, new ExplodingEffect())
                .exclusiveSet(enchantments.getOrThrow(INFLICT_EXCLUSIVE_SET))
                .build(EXPLODING.getValue()));
        registerable.register(FREEZING, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                4,
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
                .exclusiveSet(enchantments.getOrThrow(INFLICT_EXCLUSIVE_SET))
                .build(FREEZING.getValue()));
        registerable.register(GRAVITY, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                2,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(EnchantmentEffectComponentTypes.POST_ATTACK, EnchantmentEffectTarget.ATTACKER, EnchantmentEffectTarget.VICTIM, new GravityEffect())
                .exclusiveSet(enchantments.getOrThrow(INFLICT_EXCLUSIVE_SET))
                .build(GRAVITY.getValue()));
        registerable.register(BANE_OF_ILLAGERS, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SHARP_WEAPON_ENCHANTABLE),
                                6,
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
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                4,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(POST_KILL, new LeechingEffect())
                .build(LEECHING.getValue()));
        registerable.register(RAMPAGE, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                2,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(POST_KILL, new RampageEffect())
                .exclusiveSet(enchantments.getOrThrow(CONDITIONAL_EXCLUSIVE_SET))
                .build(RAMPAGE.getValue()));
        registerable.register(WEAKENING, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                4,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                )
                .addEffect(EnchantmentEffectComponentTypes.POST_ATTACK, EnchantmentEffectTarget.ATTACKER, EnchantmentEffectTarget.VICTIM, new WeakeningEffect())
                .exclusiveSet(enchantments.getOrThrow(INFLICT_EXCLUSIVE_SET))
                .build(WEAKENING.getValue()));

        registerable.register(ACROBAT, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                                2,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.FEET
                        )
                )
                .addEffect(
                        EnchantmentEffectComponentTypes.ATTRIBUTES,
                        new AttributeEnchantmentEffect(
                                id("enchantment.acrobat"),
                                PLAYER_ROLL_COOLDOWN,
                                EnchantmentLevelBasedValue.linear(-0.15f),
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        )
                )
                .exclusiveSet(enchantments.getOrThrow(ROLL_EXCLUSIVE_SET))
                .build(ACROBAT.getValue()));
        registerable.register(FIRE_TRAIL, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                                4,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.FEET
                        )
                )
                .addEffect(
                        EnchantmentEffectComponentTypes.ATTRIBUTES,
                        new AttributeEnchantmentEffect(
                                id("enchantment.fire_trail"),
                                PLAYER_FIRE_TRAIL,
                                EnchantmentLevelBasedValue.linear(0.5f, 0.25f),
                                EntityAttributeModifier.Operation.ADD_VALUE
                        )
                )
                .exclusiveSet(enchantments.getOrThrow(ROLL_EXCLUSIVE_SET))
                .build(FIRE_TRAIL.getValue()));
        registerable.register(RECKLESS, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                                2,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.FEET
                        )
                )
                .addEffect(
                        EnchantmentEffectComponentTypes.ATTRIBUTES,
                        new AttributeEnchantmentEffect(
                                id("enchantment.reckless_max_health"),
                                EntityAttributes.GENERIC_MAX_HEALTH,
                                EnchantmentLevelBasedValue.constant(-0.4f),
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        )
                )
                .addEffect(
                        EnchantmentEffectComponentTypes.ATTRIBUTES,
                        new AttributeEnchantmentEffect(
                                id("enchantment.reckless_damage"),
                                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                EnchantmentLevelBasedValue.linear(0.5f, 0.2f),
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        )
                )
                .build(RECKLESS.getValue()));
        registerable.register(SWIFT_FOOTED, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                                4,
                                3,
                                Enchantment.leveledCost(1, 11),
                                Enchantment.leveledCost(21, 11),
                                1,
                                AttributeModifierSlot.FEET
                        )
                )
                .addEffect(
                        EnchantmentEffectComponentTypes.ATTRIBUTES,
                        new AttributeEnchantmentEffect(
                                id("enchantment.swift_footed"),
                                PLAYER_SWIFT_FOOTED,
                                EnchantmentLevelBasedValue.linear(20),
                                EntityAttributeModifier.Operation.ADD_VALUE
                        )
                )
                .exclusiveSet(enchantments.getOrThrow(ROLL_EXCLUSIVE_SET))
                .build(SWIFT_FOOTED.getValue()));
    }
}

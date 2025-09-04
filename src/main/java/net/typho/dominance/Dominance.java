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
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.enchantment.effect.entity.ApplyMobEffectEnchantmentEffect;
import net.minecraft.enchantment.effect.value.MultiplyEnchantmentEffect;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.typho.dominance.client.DamageParticleEffect;
import net.typho.dominance.enchants.*;
import net.typho.dominance.gear.*;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

import java.awt.*;
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

    public static final TagKey<Item> ARMOR_REFORGABLE = TagKey.of(RegistryKeys.ITEM, id("reforgable/armor"));
    public static final TagKey<Item> HANDHELD_REFORGABLE = TagKey.of(RegistryKeys.ITEM, id("reforgable/handheld"));

    public static final RegistryKey<Registry<Reforge.Factory<?>>> REFORGE_KEY = RegistryKey.ofRegistry(id("reforge"));
    public static final ComponentType<Reforge> REFORGE_COMPONENT = Registry.register(Registries.DATA_COMPONENT_TYPE, id("reforge"), new ComponentType.Builder<Reforge>().codec(Reforge.CODEC).packetCodec(Reforge.PACKET_CODEC).build());

    public static final RegistryKey<Registry<Reforge.Type<?>>> REFORGE_TYPE_KEY = RegistryKey.ofRegistry(id("reforge_type"));
    public static final Registry<Reforge.Type<?>> REFORGE_TYPE = FabricRegistryBuilder.createSimple(REFORGE_TYPE_KEY).buildAndRegister();

    public static final RegistryEntry<Reforge.Type<?>> BASIC_REFORGE = Registry.registerReference(REFORGE_TYPE, id("basic"), (Reforge.Type<BasicReforge.Factory>) () -> BasicReforge.Factory.CODEC);

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
            1f,
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
            18,
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            Ingredient::empty,
            List.of(),
            0,
            0
    ));

    public static final RoyalGuardArmorItem ROYAL_GUARD_HELMET = item("royal_guard_helmet", new RoyalGuardArmorItem(ROYAL_GUARD_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(20)).rarity(Rarity.EPIC)));
    public static final RoyalGuardArmorItem ROYAL_GUARD_CHESTPLATE = item("royal_guard_chestplate", new RoyalGuardArmorItem(ROYAL_GUARD_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings().maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(20)).rarity(Rarity.EPIC)));
    public static final RoyalGuardArmorItem ROYAL_GUARD_BOOTS = item("royal_guard_boots", new RoyalGuardArmorItem(ROYAL_GUARD_MATERIAL, ArmorItem.Type.BOOTS, new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(20)).rarity(Rarity.EPIC)));
    public static final SplashWeaponItem ROYAL_GUARD_MACE = item("royal_guard_mace", new SplashWeaponItem(1, 7, 0.5f, new Item.Settings().maxDamage(500).rarity(Rarity.EPIC).attributeModifiers(weaponAttributes(10, -3.2))));
    public static final ShieldItem ROYAL_GUARD_SHIELD = item("royal_guard_shield", new ShieldItem(new Item.Settings().rarity(Rarity.EPIC).maxCount(1).maxDamage(504)));
    public static final EvocationRobeItem EVOCATION_HAT = item("evocation_hat", new EvocationRobeItem(EVOCATION_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(22)).rarity(Rarity.EPIC)));
    public static final EvocationRobeItem EVOCATION_ROBE = item("evocation_robe", new EvocationRobeItem(EVOCATION_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings().maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(22)).rarity(Rarity.EPIC)));
    public static final SplashWeaponItem GREAT_HAMMER = item("great_hammer", new SplashWeaponItem(3, 5, 1, new Item.Settings().maxDamage(500).rarity(Rarity.EPIC).attributeModifiers(weaponAttributes(14, -3.6))));
    public static final SwordItem KATANA = item("katana", new SwordItem(ToolMaterials.IRON, new Item.Settings().rarity(Rarity.UNCOMMON).attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 2, -2f))));
    public static final BurstCrossbowItem BURST_CROSSBOW = item("burst_crossbow", new BurstCrossbowItem(new Item.Settings().rarity(Rarity.EPIC).maxCount(1).maxDamage(465)));
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
                        new EntityAttributeModifier(id("evocation_robe_set_bonus"), 0.25, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        AttributeModifierSlot.ARMOR
                ).build());
    }

    public static final int ROLL_LENGTH = 8;
    public static final int ROLL_COOLDOWN = 60;

    public static final SpriteIdentifier ROYAL_GUARD_SHIELD_SPRITE = new SpriteIdentifier(
            TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, id("entity/royal_guard_shield")
    );
    public static final ShieldEntityModel ROYAL_GUARD_SHIELD_MODEL;

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

    public static final RegistryKey<Enchantment> AMBUSH = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("ambush"));
    public static final RegistryKey<Enchantment> COMMITTED = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("committed"));
    public static final RegistryKey<Enchantment> DYNAMO = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("dynamo"));
    public static final RegistryKey<Enchantment> EXPLODING = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("exploding"));
    public static final RegistryKey<Enchantment> FREEZING = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("freezing"));
    public static final RegistryKey<Enchantment> GRAVITY = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("gravity"));
    public static final RegistryKey<Enchantment> BANE_OF_ILLAGERS = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("bane_of_illagers"));
    public static final RegistryKey<Enchantment> LEECHING = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("leeching"));
    public static final RegistryKey<Enchantment> RAMPAGE = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("rampage"));
    public static final RegistryKey<Enchantment> WEAKENING = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("weakening"));

    public static final MapCodec<AmbushEffect> AMBUSH_EFFECT = Registry.register(ENCHANTMENT_DAMAGE_EFFECTS, id("ambush"), AmbushEffect.CODEC);
    public static final MapCodec<CommittedEffect> COMMITTED_EFFECT = Registry.register(ENCHANTMENT_DAMAGE_EFFECTS, id("committed"), CommittedEffect.CODEC);
    public static final MapCodec<DynamoEffect> DYNAMO_EFFECT = Registry.register(ENCHANTMENT_DAMAGE_EFFECTS, id("dynamo"), DynamoEffect.CODEC);
    public static final MapCodec<ExplodingEffect> EXPLODING_EFFECT = Registry.register(ENCHANTMENT_POST_KILL_EFFECTS, id("exploding"), ExplodingEffect.CODEC);
    public static final MapCodec<GravityEffect> GRAVITY_EFFECT = Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, id("gravity"), GravityEffect.CODEC);
    public static final MapCodec<LeechingEffect> LEECHING_EFFECT = Registry.register(ENCHANTMENT_POST_KILL_EFFECTS, id("leeching"), LeechingEffect.CODEC);
    public static final MapCodec<RampageEffect> RAMPAGE_EFFECT = Registry.register(ENCHANTMENT_POST_KILL_EFFECTS, id("rampage"), RampageEffect.CODEC);
    public static final MapCodec<WeakeningEffect> WEAKENING_EFFECT = Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, id("weakening"), WeakeningEffect.CODEC);

    public static final Map<Identifier, Color> EXCLUSIVE_SET_COLORS = new LinkedHashMap<>();

    public static final TagKey<Enchantment> INFLICT_EXCLUSIVE_SET = TagKey.of(RegistryKeys.ENCHANTMENT, id("exclusive_set/inflict"));
    public static final TagKey<Enchantment> CONDITIONAL_EXCLUSIVE_SET = TagKey.of(RegistryKeys.ENCHANTMENT, id("exclusive_set/conditional"));

    static {
        EXCLUSIVE_SET_COLORS.put(EnchantmentTags.RIPTIDE_EXCLUSIVE_SET.id(), new Color(160, 255, 255));
        EXCLUSIVE_SET_COLORS.put(EnchantmentTags.DAMAGE_EXCLUSIVE_SET.id(), new Color(115, 40, 40));
        EXCLUSIVE_SET_COLORS.put(EnchantmentTags.ARMOR_EXCLUSIVE_SET.id(), new Color(200, 145, 255));
        EXCLUSIVE_SET_COLORS.put(EnchantmentTags.BOOTS_EXCLUSIVE_SET.id(), new Color(0, 145, 255));
        EXCLUSIVE_SET_COLORS.put(EnchantmentTags.BOW_EXCLUSIVE_SET.id(), new Color(0, 165, 130));
        EXCLUSIVE_SET_COLORS.put(EnchantmentTags.CROSSBOW_EXCLUSIVE_SET.id(), new Color(0, 110, 130));
        EXCLUSIVE_SET_COLORS.put(EnchantmentTags.MINING_EXCLUSIVE_SET.id(), new Color(255, 230, 0));
        EXCLUSIVE_SET_COLORS.put(INFLICT_EXCLUSIVE_SET.id(), new Color(170, 45, 45));
        EXCLUSIVE_SET_COLORS.put(CONDITIONAL_EXCLUSIVE_SET.id(), new Color(145, 50, 115));
    }

    public static final RegistryKey<ItemGroup> CREATIVE_TAB_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, id("tab"));
    public static final ItemGroup CREATIVE_TAB = Registry.register(Registries.ITEM_GROUP, CREATIVE_TAB_KEY, FabricItemGroup.builder()
            .icon(() -> new ItemStack(GREAT_HAMMER))
            .displayName(Text.translatable("itemGroup.dominance"))
            .build());

    @Override
    @SuppressWarnings("unchecked")
    public void onInitialize() {
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
                data.setTime(ROLL_LENGTH);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(CREATIVE_TAB_KEY).register(entries -> {
            entries.add(ROYAL_GUARD_HELMET);
            entries.add(ROYAL_GUARD_CHESTPLATE);
            entries.add(ROYAL_GUARD_BOOTS);
            entries.add(ROYAL_GUARD_SHIELD);
            entries.add(EVOCATION_HAT);
            entries.add(EVOCATION_ROBE);
            entries.add(ROYAL_GUARD_MACE);
            entries.add(GREAT_HAMMER);
            entries.add(KATANA);
            entries.add(BURST_CROSSBOW);
            entries.add(HUNTING_BOW);
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
                                        context.getSource().sendFeedback(() -> Text.translatable("command.dominance.reforge.no_reforge", key).formatted(Formatting.RED), false);
                                        return 0;
                                    }

                                    if (!reforge.isValidItem(stack)) {
                                        context.getSource().sendFeedback(() -> Text.translatable("command.dominance.reforge.bad_item", key, stack.getItem().getName()).formatted(Formatting.RED), false);
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
                                                context.getSource().sendFeedback(() -> Text.translatable("command.dominance.reforge.no_reforge", key).formatted(Formatting.RED), false);
                                                return 0;
                                            }

                                            if (!reforge.isValidItem(stack)) {
                                                context.getSource().sendFeedback(() -> Text.translatable("command.dominance.reforge.bad_item", key, stack.getItem().getName()).formatted(Formatting.RED), false);
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

    public static void attack(PlayerEntity player, Entity target) {
        float damage = player.isUsingRiptide() ? player.riptideAttackDamage : (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        ItemStack weapon = player.getWeaponStack();
        DamageSource source = player.getDamageSources().playerAttack(player);
        damage += weapon.getItem().getBonusAttackDamage(target, damage, source);
        damage = player.getDamageAgainst(target, damage, source);
        damage *= player.getAttackCooldownProgress(0.5f);
        player.resetLastAttackedTicks();

        if (target.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE)
                && target instanceof ProjectileEntity proj
                && proj.deflect(ProjectileDeflection.REDIRECTED, player, player, true)) {
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory());
        } else {
            if (damage > 0) {
                boolean knockback;

                if (player.isSprinting()) {
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0F, 1.0F);
                    knockback = true;
                } else {
                    knockback = false;
                }

                boolean crit = player.fallDistance > 0.0F
                        && !player.isOnGround()
                        && !player.isClimbing()
                        && !player.isTouchingWater()
                        && !player.hasStatusEffect(StatusEffects.BLINDNESS)
                        && !player.hasVehicle()
                        && target instanceof LivingEntity;
                if (crit) {
                    damage *= 1.25f;
                }

                boolean sweep = false;
                double d = player.horizontalSpeed - player.prevHorizontalSpeed;

                if (!crit && !knockback && player.isOnGround() && d < player.getMovementSpeed()) {
                    ItemStack itemStack2 = player.getStackInHand(Hand.MAIN_HAND);

                    if (itemStack2.getItem() instanceof SwordItem) {
                        sweep = true;
                    }
                }

                float hp = 0.0F;
                if (target instanceof LivingEntity livingEntity) {
                    hp = livingEntity.getHealth();
                }

                Vec3d vec3d = target.getVelocity();
                boolean bl5 = target.damage(source, damage);

                if (bl5) {
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        ServerPlayNetworking.send(serverPlayer, new DamageParticleS2C(target, serverPlayer, damage));
                    }

                    float k = player.getKnockbackAgainst(target, source) + (knockback ? 1.0F : 0.0F);

                    if (k > 0.0F) {
                        if (target instanceof LivingEntity livingEntity2) {
                            livingEntity2.takeKnockback(
                                    k * 0.5F, MathHelper.sin(player.getYaw() * (float) (Math.PI / 180.0)), -MathHelper.cos(player.getYaw() * (float) (Math.PI / 180.0))
                            );
                        } else {
                            target.addVelocity(
                                    -MathHelper.sin(player.getYaw() * (float) (Math.PI / 180.0)) * k * 0.5F, 0.1, MathHelper.cos(player.getYaw() * (float) (Math.PI / 180.0)) * k * 0.5F
                            );
                        }

                        player.setVelocity(player.getVelocity().multiply(0.6, 1.0, 0.6));
                        player.setSprinting(false);
                    }

                    if (sweep) {
                        for (LivingEntity livingEntity3 : player.getWorld().getNonSpectatingEntities(LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0))) {
                            if (livingEntity3 != player
                                    && livingEntity3 != target
                                    && !player.isTeammate(livingEntity3)
                                    && (!(livingEntity3 instanceof ArmorStandEntity) || !((ArmorStandEntity)livingEntity3).isMarker())
                                    && player.squaredDistanceTo(livingEntity3) < 9.0) {
                                livingEntity3.takeKnockback(
                                        0.4F, MathHelper.sin(player.getYaw() * (float) (Math.PI / 180.0)), -MathHelper.cos(player.getYaw() * (float) (Math.PI / 180.0))
                                );

                                if (livingEntity3.damage(source, damage * (float) player.getAttributeValue(EntityAttributes.PLAYER_SWEEPING_DAMAGE_RATIO)) && player instanceof ServerPlayerEntity serverPlayer) {
                                    ServerPlayNetworking.send(serverPlayer, new DamageParticleS2C(target, serverPlayer, damage));
                                }

                                if (player.getWorld() instanceof ServerWorld serverWorld) {
                                    EnchantmentHelper.onTargetDamaged(serverWorld, livingEntity3, source);
                                }
                            }
                        }

                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
                        player.spawnSweepAttackParticles();
                    }

                    if (target instanceof ServerPlayerEntity && target.velocityModified) {
                        ((ServerPlayerEntity)target).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));
                        target.velocityModified = false;
                        target.setVelocity(vec3d);
                    }

                    if (damage > 30) {
                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                        player.addCritParticles(target);
                    } else if (crit) {
                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                        player.addCritParticles(target);
                    } else {
                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                    }

                    player.onAttacking(target);
                    Entity entity = target;
                    if (target instanceof EnderDragonPart) {
                        entity = ((EnderDragonPart)target).owner;
                    }

                    boolean bl6 = false;
                    if (player.getWorld() instanceof ServerWorld serverWorld2) {
                        if (entity instanceof LivingEntity livingEntity3x) {
                            bl6 = weapon.postHit(livingEntity3x, player);
                        }

                        EnchantmentHelper.onTargetDamaged(serverWorld2, target, source);
                    }

                    if (!player.getWorld().isClient && !weapon.isEmpty() && entity instanceof LivingEntity) {
                        if (bl6) {
                            weapon.postDamageEntity((LivingEntity) entity, player);
                        }

                        if (weapon.isEmpty()) {
                            if (weapon == player.getMainHandStack()) {
                                player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                            } else {
                                player.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
                            }
                        }
                    }

                    if (target instanceof LivingEntity) {
                        float n = hp - ((LivingEntity)target).getHealth();
                        player.increaseStat(Stats.DAMAGE_DEALT, Math.round(n * 10.0F));
                        if (player.getWorld() instanceof ServerWorld && n > 2.0F) {
                            int o = (int)(n * 0.5);
                            ((ServerWorld)player.getWorld())
                                    .spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.5), target.getZ(), o, 0.1, 0.0, 0.1, 0.2);
                        }
                    }

                    player.addExhaustion(0.1F);
                } else {
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);
                }
            }
        }
    }

    public static void reforges(Registerable<Reforge.Factory<?>> registerable) {
        RegistryKey<Reforge.Factory<?>> vitality = RegistryKey.of(REFORGE_KEY, id("vitality"));
        registerable.register(vitality, new BasicReforge.Factory(
                vitality,
                1,
                0,
                4,
                EntityAttributes.GENERIC_MAX_HEALTH,
                EntityAttributeModifier.Operation.ADD_VALUE,
                new Color(150, 50, 50),
                new Color(250, 50, 50),
                ARMOR_REFORGABLE
        ));
    }

    public static void enchantments(Registerable<Enchantment> registerable) {
        RegistryEntryLookup<Item> items = registerable.getRegistryLookup(RegistryKeys.ITEM);
        RegistryEntryLookup<Enchantment> enchantments = registerable.getRegistryLookup(RegistryKeys.ENCHANTMENT);

        registerable.register(AMBUSH, Enchantment.builder(
                        Enchantment.definition(
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
                .exclusiveSet(enchantments.getOrThrow(CONDITIONAL_EXCLUSIVE_SET))
                .build(AMBUSH.getValue()));
        registerable.register(COMMITTED, Enchantment.builder(
                        Enchantment.definition(
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
                .exclusiveSet(enchantments.getOrThrow(CONDITIONAL_EXCLUSIVE_SET))
                .build(COMMITTED.getValue()));
        registerable.register(DYNAMO, Enchantment.builder(
                        Enchantment.definition(
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
                .exclusiveSet(enchantments.getOrThrow(INFLICT_EXCLUSIVE_SET))
                .build(EXPLODING.getValue()));
        registerable.register(FREEZING, Enchantment.builder(
                        Enchantment.definition(
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
                .exclusiveSet(enchantments.getOrThrow(INFLICT_EXCLUSIVE_SET))
                .build(FREEZING.getValue()));
        registerable.register(GRAVITY, Enchantment.builder(
                        Enchantment.definition(
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
                .exclusiveSet(enchantments.getOrThrow(INFLICT_EXCLUSIVE_SET))
                .build(GRAVITY.getValue()));
        registerable.register(BANE_OF_ILLAGERS, Enchantment.builder(
                        Enchantment.definition(
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
        registerable.register(RAMPAGE, Enchantment.builder(
                        Enchantment.definition(
                                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                3,
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
                                3,
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
    }
}

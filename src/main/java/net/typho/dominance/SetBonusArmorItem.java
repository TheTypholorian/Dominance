package net.typho.dominance;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;

public abstract class SetBonusArmorItem extends ArmorItem implements GeoItem {
    public SetBonusArmorItem(RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
        super(material, type, settings);
    }

    /*
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        PlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null) {
            List<AttributeModifiersComponent> effects = Dominance.ARMOR_SET_BONUSES.entrySet().stream().filter(entry -> {
                for (ArmorItem item : entry.getKey()) {
                    if (!player.getEquippedStack(item.getSlotType()).isOf(item)) {
                        return false;
                    }
                }

                return true;
            }).map(Map.Entry::getValue).toList();

            if (!effects.isEmpty()) {
                tooltip.add(Text.translatable("tooltip.dominance.set_bonus").formatted(Formatting.GRAY));

                for (AttributeModifiersComponent effect : effects) {
                    effect.getEffectType().value().forEachAttributeModifier(effect.getAmplifier(), (attribute, modifier) -> {
                        double d = modifier.value();
                        double e;
                        if (modifier.operation() != EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                                && modifier.operation() != EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                            e = modifier.value();
                        } else {
                            e = modifier.value() * 100.0;
                        }

                        if (d > 0.0) {
                            tooltip.add(
                                    Text.translatable(
                                                    "attribute.modifier.plus." + modifier.operation().getId(),
                                                    AttributeModifiersComponent.DECIMAL_FORMAT.format(e),
                                                    Text.translatable(attribute.value().getTranslationKey())
                                            )
                                            .formatted(Formatting.BLUE)
                            );
                        } else if (d < 0.0) {
                            e *= -1.0;
                            tooltip.add(
                                    Text.translatable(
                                                    "attribute.modifier.take." + modifier.operation().getId(),
                                                    AttributeModifiersComponent.DECIMAL_FORMAT.format(e),
                                                    Text.translatable(attribute.value().getTranslationKey())
                                            )
                                            .formatted(Formatting.RED)
                            );
                        }
                    });
                }
            }
        }
    }
     */

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof PlayerEntity player) {
            Dominance.ARMOR_SET_BONUSES.forEach((set, effect) -> {
                for (ArmorItem item : set) {
                    if (!player.getEquippedStack(item.getSlotType()).isOf(item)) {
                        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> map = HashMultimap.create();
                        effect.applyModifiers(getSlotType(), map::put);
                        player.getAttributes().removeModifiers(map);
                        return;
                    }
                }

                Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> map = HashMultimap.create();
                effect.applyModifiers(getSlotType(), map::put);
                player.getAttributes().addTemporaryModifiers(map);
            });
        }
    }
}

package net.typho.dominance;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.LinkedList;
import java.util.List;

public abstract class SetBonusArmorItem extends ArmorItem implements GeoItem {
    public SetBonusArmorItem(RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
        super(material, type, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        PlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null) {
            List<AttributeModifiersComponent> bonuses = new LinkedList<>();

            Dominance.ARMOR_SET_BONUSES.forEach((set, effect) -> {
                for (ArmorItem item : set) {
                    if (!player.getEquippedStack(item.getSlotType()).isOf(item)) {
                        return;
                    }
                }

                bonuses.add(effect);
            });

            if (!bonuses.isEmpty()) {
                tooltip.add(Text.translatable("tooltip.dominance.set_bonus").formatted(Formatting.GRAY));

                for (AttributeModifiersComponent effect : bonuses) {
                    effect.applyModifiers(getSlotType(), (attribute, modifier) -> stack.appendAttributeModifierTooltip(tooltip::add, player, attribute, modifier));
                }
            }
        }
    }

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

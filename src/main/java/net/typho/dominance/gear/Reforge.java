package net.typho.dominance.gear;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.typho.dominance.Dominance;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public interface Reforge {
    Codec<Reforge> CODEC = Identifier.CODEC.dispatch(
            Reforge::id,
            id -> Dominance.REFORGES.getOrEmpty(id).map(Factory::codec).orElse(null)
    );
    PacketCodec<ByteBuf, Reforge> PACKET_CODEC = Identifier.PACKET_CODEC.dispatch(
            Reforge::id,
            id -> Dominance.REFORGES.getOrEmpty(id).map(Factory::packetCodec).orElse(null)
    );

    List<AttributeModifiersComponent.Entry> modifiers(ItemStack stack);

    Identifier id();

    Text name(ItemStack stack, Text name);

    default AttributeModifiersComponent.Entry modifierForStack(RegistryEntry<EntityAttribute> attrib, double value, EntityAttributeModifier.Operation op, ItemStack stack) {
        return modifierForStack(attrib, value, op, stack, false);
    }

    default AttributeModifiersComponent.Entry modifierForStack(RegistryEntry<EntityAttribute> attrib, double value, EntityAttributeModifier.Operation op, ItemStack stack, boolean compact) {
        AttributeModifierSlot slot = slotForStack(stack);
        return new AttributeModifiersComponent.Entry(
                attrib,
                new EntityAttributeModifier(compact ? id().withSuffixedPath("/" + slot.asString()) : id().withSuffixedPath(attrib.getKey().map(key -> "/" + key.getValue().toShortTranslationKey()).orElse("") + "/" + slot.asString()), value, op),
                slot
        );
    }

    static AttributeModifierSlot slotForStack(ItemStack stack) {
        if (stack.getItem() instanceof Equipment equipment) {
            return AttributeModifierSlot.forEquipmentSlot(equipment.getSlotType());
        } else {
            return AttributeModifierSlot.HAND;
        }
    }

    static Color blendColors(Color min, Color max, double delta) {
        delta = MathHelper.clamp(delta, 0, 1);

        return new Color(
                (int) ((max.getRed() - min.getRed()) * delta + min.getRed()),
                (int) ((max.getGreen() - min.getGreen()) * delta + min.getGreen()),
                (int) ((max.getBlue() - min.getBlue()) * delta + min.getBlue()),
                (int) ((max.getAlpha() - min.getAlpha()) * delta + min.getAlpha())
        );
    }

    static Factory<?> pickForStack(ItemStack stack) {
        int total = 0;
        List<Factory<?>> options = new LinkedList<>();

        for (Factory<?> factory : Dominance.REFORGES) {
            if (factory.isValidItem(stack)) {
                options.add(factory);
                total += factory.weight();
            }
        }

        if (total == 0) {
            return null;
        }

        Factory<?>[] arr = new Factory[total];
        int i = 0;

        for (Factory<?> factory : options) {
            for (int j = 0; j < factory.weight(); j++, i++) {
                arr[i] = factory;
            }
        }

        return arr[new Random().nextInt(total)];
    }

    interface Factory<R extends Reforge> {
        MapCodec<R> codec();

        PacketCodec<ByteBuf, R> packetCodec();

        int weight();

        boolean isValidItem(ItemStack stack);

        R generate(ItemStack stack);
    }
}

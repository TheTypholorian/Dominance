package net.typho.dominance.gear;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.typho.dominance.Dominance;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public interface Reforge {
    Codec<Color> COLOR_CODEC = Codec.either(
            Codec.either(
                    Codec.INT.xmap(Color::new, Color::getRGB),
                    Codec.INT.listOf(3, 4).xmap(list -> list.size() == 3 ? new Color(list.getFirst(), list.get(1), list.get(2)) : new Color(list.getFirst(), list.get(1), list.get(2), list.get(3)), color -> List.of(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()))
            ),
            Codec.FLOAT.listOf(3, 4).xmap(list -> list.size() == 3 ? new Color(list.getFirst(), list.get(1), list.get(2)) : new Color(list.getFirst(), list.get(1), list.get(2), list.get(3)), color -> List.of(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f))
    ).xmap(
            either -> either.map(
                    color -> color.map(
                            color1 -> color1,
                            color1 -> color1
                    ),
                    inner -> inner
            ),
            color -> Either.left(Either.right(color))
    );

    Codec<Reforge> CODEC = RegistryFixedCodec.of(Dominance.REFORGE_KEY)
            .fieldOf("type")
            .codec()
            .dispatch(Reforge::factory, key -> key.value().codec());
    PacketCodec<RegistryByteBuf, Reforge> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    List<AttributeModifiersComponent.Entry> modifiers(ItemStack stack);

    Identifier id();

    Text name(ItemStack stack, Text name);

    RegistryEntry<Factory<?>> factory();

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

    static Factory<?> pickForStack(ItemStack stack, DynamicRegistryManager registries) {
        int total = 0;
        List<Factory<?>> options = new LinkedList<>();

        for (Factory<?> factory : registries.get(Dominance.REFORGE_KEY)) {
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
        Codec<Factory<?>> CODEC = RegistryKey.createCodec(Dominance.REFORGE_TYPE_KEY)
                .fieldOf("type")
                .codec()
                .dispatch(Factory::type, key -> Dominance.REFORGE_TYPE.getOrEmpty(key).map(Type::codec).orElse(null));

        MapCodec<R> codec();

        RegistryKey<Type<?>> type();

        int weight();

        boolean isValidItem(ItemStack stack);

        R generate(ItemStack stack);
    }

    interface Type<F extends Factory<?>> {
        MapCodec<F> codec();
    }
}

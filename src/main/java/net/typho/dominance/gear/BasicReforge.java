package net.typho.dominance.gear;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.MathHelper;
import net.typho.dominance.Dominance;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public final class BasicReforge implements Reforge {
    public final Factory factory;
    public final double value;

    public BasicReforge(Factory factory, double value) {
        this.factory = factory;
        this.value = MathHelper.clamp(value, 0, 1);
    }

    @Override
    public List<AttributeModifiersComponent.Entry> modifiers(ItemStack stack) {
        List<AttributeModifiersComponent.Entry> modifiers = new LinkedList<>();

        for (Entry entry : factory.attributes) {
            modifiers.add(modifierForStack(entry.attribute, entry.get(value), entry.op, stack));
        }

        return modifiers;
    }

    @Override
    public Identifier id() {
        return factory.key.getValue();
    }

    @Override
    public RegistryKey<Reforge.Factory<?>> factory() {
        return factory.key;
    }

    @Override
    public Text name(ItemStack stack, Text name) {
        return Text.translatable(id().toTranslationKey("reforge"), name);
    }

    public final static class Factory implements Reforge.Factory<BasicReforge> {
        public static final MapCodec<Factory> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RegistryKey.createCodec(Dominance.REFORGE_KEY).fieldOf("key").forGetter(f -> f.key),
                Rarity.CODEC.fieldOf("rarity").forGetter(f -> f.rarity),
                Entry.CODEC.codec().listOf().fieldOf("attributes").forGetter(f -> f.attributes),
                TagKey.codec(RegistryKeys.ITEM).fieldOf("tag").forGetter(f -> f.tag)
        ).apply(instance, Factory::new));

        public final RegistryKey<Reforge.Factory<?>> key;
        public final MapCodec<BasicReforge> codec;
        public final Rarity rarity;
        public final List<Entry> attributes;
        public final TagKey<Item> tag;

        public Factory(RegistryKey<Reforge.Factory<?>> key, Rarity rarity, List<Entry> attributes, TagKey<Item> tag) {
            codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.DOUBLE.fieldOf("value").forGetter(simple -> simple.value)
            ).apply(instance, value -> new BasicReforge(Factory.this, value)));
            this.key = key;
            this.rarity = rarity;
            this.attributes = attributes;
            this.tag = tag;
        }

        @Override
        public MapCodec<BasicReforge> codec() {
            return codec;
        }

        @Override
        public RegistryKey<Type<?>> type() {
            return Dominance.ATTRIBUTES_REFORGE_TYPE.getKey().orElseThrow();
        }

        @Override
        public Rarity rarity() {
            return rarity;
        }

        @Override
        public boolean isValidItem(ItemStack stack) {
            return stack.isIn(tag);
        }

        @Override
        public BasicReforge generate(ItemStack stack) {
            return new BasicReforge(this, MathHelper.clamp(new Random().nextDouble() * stack.getItem().getEnchantability() / 20f, 0, 1));
        }
    }

    public record Entry(RegistryEntry<EntityAttribute> attribute, double min, double max, double increment, EntityAttributeModifier.Operation op) {
        public static final MapCodec<Entry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                EntityAttribute.CODEC.fieldOf("attribute").forGetter(f -> f.attribute),
                Codec.DOUBLE.optionalFieldOf("min", 0d).forGetter(f -> f.min),
                Codec.DOUBLE.fieldOf("max").forGetter(f -> f.max),
                Codec.DOUBLE.optionalFieldOf("increment", 0.25).forGetter(f -> f.increment),
                EntityAttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(f -> f.op)
        ).apply(instance, Entry::new));

        public double get(double value) {
            return Math.round((value * (max - min) + min) / increment) * increment;
        }
    }
}

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
import net.minecraft.util.math.MathHelper;
import net.typho.dominance.Dominance;

import java.awt.*;
import java.util.List;
import java.util.Random;

public final class BasicReforge implements Reforge {
    public final Factory factory;
    public final double value;

    public BasicReforge(Factory factory, double value) {
        this.factory = factory;
        this.value = value;
        System.out.println("Created basic reforge " + factory.attribute + " " + id());
    }

    @Override
    public List<AttributeModifiersComponent.Entry> modifiers(ItemStack stack) {
        return List.of(modifierForStack(factory.attribute, value, factory.op, stack, true));
    }

    @Override
    public Identifier id() {
        return factory.key.getValue();
    }

    @Override
    public RegistryKey<Reforge.Factory<?>> factory() {
        return factory.key;
        //return RegistryEntry.Reference.standAlone(Dominance.REFORGE_KEY, factory.key);
    }

    public int getColor() {
        return Reforge.blendColors(factory.minColor, factory.maxColor, value / factory.max).getRGB();
    }

    @Override
    public Text name(ItemStack stack, Text name) {
        return Text.translatable(id().toTranslationKey("reforge")).styled(style -> style.withColor(getColor())).append(" ").append(name);
    }

    public final static class Factory implements Reforge.Factory<BasicReforge> {
        public static final MapCodec<Factory> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RegistryKey.createCodec(Dominance.REFORGE_KEY).fieldOf("key").forGetter(f -> f.key),
                Codec.INT.fieldOf("weight").forGetter(f -> f.weight),
                Codec.DOUBLE.fieldOf("min").forGetter(f -> f.min),
                Codec.DOUBLE.fieldOf("max").forGetter(f -> f.max),
                EntityAttribute.CODEC.fieldOf("attribute").forGetter(f -> f.attribute),
                EntityAttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(f -> f.op),
                Reforge.COLOR_CODEC.fieldOf("min_color").forGetter(f -> f.minColor),
                Reforge.COLOR_CODEC.fieldOf("max_color").forGetter(f -> f.maxColor),
                TagKey.codec(RegistryKeys.ITEM).fieldOf("tag").forGetter(f -> f.tag)
        ).apply(instance, Factory::new));

        public final RegistryKey<Reforge.Factory<?>> key;
        public final MapCodec<BasicReforge> codec;
        public final int weight;
        public final double min, max;
        public final RegistryEntry<EntityAttribute> attribute;
        public final EntityAttributeModifier.Operation op;
        public final Color minColor, maxColor;
        public final TagKey<Item> tag;

        public Factory(RegistryKey<Reforge.Factory<?>> key, int weight, double min, double max, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation op, Color minColor, Color maxColor, TagKey<Item> tag) {
            codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.DOUBLE.fieldOf("value").forGetter(simple -> simple.value)
            ).apply(instance, value -> new BasicReforge(Factory.this, value)));
            this.key = key;
            this.weight = weight;
            this.min = min;
            this.max = max;
            this.attribute = attribute;
            this.op = op;
            this.minColor = minColor;
            this.maxColor = maxColor;
            this.tag = tag;
        }

        @Override
        public MapCodec<BasicReforge> codec() {
            return codec;
        }

        @Override
        public RegistryKey<Type<?>> type() {
            return Dominance.BASIC_REFORGE.getKey().orElseThrow();
        }

        @Override
        public int weight() {
            return weight;
        }

        @Override
        public boolean isValidItem(ItemStack stack) {
            return stack.isIn(tag);
        }

        @Override
        public BasicReforge generate(ItemStack stack) {
            return new BasicReforge(this, new Random().nextDouble() * MathHelper.clamp(stack.getItem().getEnchantability() / 20f, 0, 1) * (max - min) + min);
        }
    }
}

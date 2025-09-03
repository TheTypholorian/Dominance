package net.typho.dominance.gear;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class SimpleReforge implements Reforge {
    public final Factory factory;
    public final double value;

    public SimpleReforge(Factory factory, double value) {
        this.factory = factory;
        this.value = value;
    }

    @Override
    public List<AttributeModifiersComponent.Entry> modifiers(ItemStack stack) {
        return List.of(modifierForStack(factory.attribute, value, factory.op, stack, true));
    }

    @Override
    public Identifier id() {
        return factory.id;
    }

    public int getColor() {
        return Reforge.blendColors(factory.badColor, factory.goodColor, value / factory.max).getRGB();
    }

    @Override
    public Text name(ItemStack stack, Text name) {
        return Text.translatable(id().toTranslationKey("reforge", "name")).styled(style -> style.withColor(getColor())).append(" ").append(name);
    }

    public abstract static class Factory implements Reforge.Factory<SimpleReforge> {
        public final Identifier id;
        public final MapCodec<SimpleReforge> codec;
        public final PacketCodec<ByteBuf, SimpleReforge> packetCodec;
        public final int weight;
        public final double max;
        public final RegistryEntry<EntityAttribute> attribute;
        public final EntityAttributeModifier.Operation op;
        public final Color badColor, goodColor;
        public final TagKey<Item> tag;

        public Factory(Identifier id, int weight, double max, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation op, Color badColor, Color goodColor, TagKey<Item> tag) {
            this.id = id;
            codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.DOUBLE.fieldOf("value").forGetter(simple -> simple.value)
            ).apply(instance, value -> new SimpleReforge(Factory.this, value)));
            packetCodec = PacketCodec.tuple(
                    PacketCodecs.DOUBLE, simple -> simple.value,
                    value -> new SimpleReforge(Factory.this, value)
            );
            this.weight = weight;
            this.max = max;
            this.attribute = attribute;
            this.op = op;
            this.badColor = badColor;
            this.goodColor = goodColor;
            this.tag = tag;
        }

        @Override
        public MapCodec<SimpleReforge> codec() {
            return codec;
        }

        @Override
        public PacketCodec<ByteBuf, SimpleReforge> packetCodec() {
            return packetCodec;
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
        public SimpleReforge generate(ItemStack stack) {
            return new SimpleReforge(this, new Random().nextDouble() * MathHelper.clamp(stack.getItem().getEnchantability() / 20f, 0, 1) * max);
        }
    }
}

package net.typho.dominance.gear;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import net.typho.dominance.Dominance;

import java.awt.*;

public class LightweightReforge extends SimpleReforge.Factory {
    public static final Identifier ID = Dominance.id("lightweight");

    public LightweightReforge() {
        super(
                ID,
                2,
                0.1,
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                new Color(115, 115, 155),
                new Color(215, 215, 255),
                Dominance.ARMOR_REFORGABLE
        );
    }
}

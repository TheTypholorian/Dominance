package net.typho.dominance.gear;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import net.typho.dominance.Dominance;

import java.awt.*;

public class VitalityReforge extends BasicReforge.Factory {
    public static final Identifier ID = Dominance.id("vitality");

    public VitalityReforge() {
        super(
                ID,
                1,
                4,
                EntityAttributes.GENERIC_MAX_HEALTH,
                EntityAttributeModifier.Operation.ADD_VALUE,
                new Color(150, 50, 50),
                new Color(250, 50, 50),
                Dominance.ARMOR_REFORGABLE
        );
    }
}

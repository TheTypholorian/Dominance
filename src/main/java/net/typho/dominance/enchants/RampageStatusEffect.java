package net.typho.dominance.enchants;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleEffect;
import net.typho.dominance.Dominance;

public class RampageStatusEffect extends StatusEffect {
    public RampageStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    public RampageStatusEffect(StatusEffectCategory category, int color, ParticleEffect particleEffect) {
        super(category, color, particleEffect);
    }

    {
        addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, Dominance.id("effect.rampage"), 0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}

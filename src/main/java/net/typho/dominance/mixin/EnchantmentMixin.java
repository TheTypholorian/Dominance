package net.typho.dominance.mixin;

import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.typho.dominance.Dominance;
import net.typho.dominance.enchants.DamageModifier;
import net.typho.dominance.enchants.EnchantmentModifyDamageEffect;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {
    @Shadow
    public abstract <T> List<T> getEffect(ComponentType<List<T>> type);

    @Inject(
            method = "modifyDamage",
            at = @At("TAIL")
    )
    private void onTargetDamaged(ServerWorld world, int level, ItemStack stack, Entity user, DamageSource damageSource, MutableFloat damage, CallbackInfo ci) {
        System.out.println("Base damage " + damage);

        List<DamageModifier> modifiers = new LinkedList<>();

        for (EnchantmentEffectEntry<EnchantmentModifyDamageEffect> effect : getEffect(Dominance.MODIFY_DAMAGE)) {
            if (effect.test(Enchantment.createEnchantedDamageLootContext(world, level, user, damageSource))) {
                DamageModifier mod = effect.effect().apply(world, level, stack, user, damageSource, user.getPos());

                if (mod != null) {
                    modifiers.add(mod);
                }
            }
        }

        modifiers.sort(Comparator.comparingInt(modifier -> modifier.op().ordinal()));

        for (DamageModifier modifier : modifiers) {
            modifier.accept(damage);
            System.out.println("Now " + damage + " because " + modifier);
        }
    }
}

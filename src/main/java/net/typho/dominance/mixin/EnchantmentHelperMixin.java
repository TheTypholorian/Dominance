package net.typho.dominance.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.typho.dominance.Dominance;
import net.typho.dominance.gear.BurstCrossbowItem;
import net.typho.dominance.gear.Reforge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @ModifyReturnValue(
            method = "getProjectileCount",
            at = @At("TAIL")
    )
    private static int getProjectileCount(int original, @Local(argsOnly = true) ItemStack stack) {
        return stack.getItem() instanceof BurstCrossbowItem burst ? burst.extraProjectiles() + original : original;
    }

    @Inject(
            method = "applyAttributeModifiers(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("TAIL")
    )
    private static void applyAttributeModifiers(ItemStack stack, EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> output, CallbackInfo ci) {
        Reforge reforge = stack.get(Dominance.REFORGE_COMPONENT);

        if (reforge != null) {
            for (AttributeModifiersComponent.Entry entry : reforge.modifiers(stack)) {
                if (entry.slot().matches(slot)) {
                    output.accept(entry.attribute(), entry.modifier());
                }
            }
        }
    }

    @Inject(
            method = "applyAttributeModifiers(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("TAIL")
    )
    private static void applyAttributeModifiers(ItemStack stack, AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> output, CallbackInfo ci) {
        Reforge reforge = stack.get(Dominance.REFORGE_COMPONENT);

        if (reforge != null) {
            for (AttributeModifiersComponent.Entry entry : reforge.modifiers(stack)) {
                if (entry.slot() == slot) {
                    output.accept(entry.attribute(), entry.modifier());
                }
            }
        }
    }

    @ModifyConstant(
            method = "calculateRequiredExperienceLevel",
            constant = @Constant(intValue = 15)
    )
    private static int maxBookshelves(int constant) {
        return constant + 5;
    }
}

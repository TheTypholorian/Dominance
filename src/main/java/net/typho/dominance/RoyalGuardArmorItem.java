package net.typho.dominance;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.typho.dominance.client.RoyalGuardArmorRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.LinkedList;
import java.util.function.Consumer;

public class RoyalGuardArmorItem extends SetBonusArmorItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RoyalGuardArmorItem(RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
        super(material, type, settings);
    }

    @Override
    public AttributeModifiersComponent getAttributeModifiers() {
        AttributeModifiersComponent modifiers = new AttributeModifiersComponent(new LinkedList<>(), true);
        modifiers.modifiers().addAll(super.getAttributeModifiers().modifiers());
        modifiers.modifiers().add(new AttributeModifiersComponent.Entry(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                new EntityAttributeModifier(Identifier.of(Dominance.MOD_ID, "royal_guard_slow_speed_" + type.getName()), -0.1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                AttributeModifierSlot.ARMOR
        ));
        return modifiers;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public <T extends LivingEntity> @NotNull BipedEntityModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot, @Nullable BipedEntityModel<T> original) {
                if (renderer == null) {
                    renderer = new RoyalGuardArmorRenderer(type.getName());
                }

                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}

package net.typho.dominance;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RoyalGuardEntity extends VindicatorEntity {
    public RoyalGuardEntity(EntityType<? extends VindicatorEntity> entityType, World world) {
        super(entityType, world);
    }

    protected ItemStack enchantForRaid(World world, @Nullable Raid raid, int wave, ItemStack stack) {
        if (random.nextFloat() <= (raid == null ? 0.5 : raid.getEnchantmentChance())) {
            EnchantmentHelper.enchant(random, stack, random.nextInt(10) + 15 + wave, world.getRegistryManager(), Optional.of((RegistryEntryList<Enchantment>) world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(EnchantmentTags.ON_RANDOM_LOOT)));
        }

        return stack;
    }

    protected void initEquipment(World world, int wave) {
        equipStack(EquipmentSlot.MAINHAND, enchantForRaid(world, raid, wave, new ItemStack(Dominance.ROYAL_GUARD_MACE)));
        equipStack(EquipmentSlot.OFFHAND, enchantForRaid(world, raid, wave, new ItemStack(Dominance.ROYAL_GUARD_SHIELD)));
        equipStack(EquipmentSlot.HEAD, enchantForRaid(world, raid, wave, new ItemStack(Dominance.ROYAL_GUARD_HELMET)));
        equipStack(EquipmentSlot.CHEST, enchantForRaid(world, raid, wave, new ItemStack(Dominance.ROYAL_GUARD_CHESTPLATE)));
        equipStack(EquipmentSlot.FEET, enchantForRaid(world, raid, wave, new ItemStack(Dominance.ROYAL_GUARD_BOOTS)));
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (getRaid() == null) {
            initEquipment(getWorld(), 0);
        }
    }

    @Override
    public void addBonusForWave(ServerWorld world, int wave, boolean unused) {
        Raid raid = getRaid();

        if (raid != null) {
            initEquipment(getWorld(), wave);
        }
    }

    public static DefaultAttributeContainer.Builder createRoyalGuardAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 12)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        ModelPartData head = root.addChild(
                EntityModelPartNames.HEAD,
                ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );

        root.addChild(
                EntityModelPartNames.HAT,
                ModelPartBuilder.create().uv(32, 0).cuboid(-4.0F, -10.0F, -4.0F, 8.0F, 12.0F, 8.0F, new Dilation(0.45F)),
                ModelTransform.NONE
        );

        head.addChild(
                EntityModelPartNames.NOSE,
                ModelPartBuilder.create().uv(24, 0).cuboid(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F),
                ModelTransform.pivot(0.0F, -2.0F, 0.0F)
        );

        root.addChild(
                EntityModelPartNames.BODY,
                ModelPartBuilder.create()
                        .uv(16, 20).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
                        .uv(0, 38).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 20.0F, 6.0F, new Dilation(0.5F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );

        ModelPartData arms = root.addChild(
                EntityModelPartNames.ARMS,
                ModelPartBuilder.create().uv(44, 22).cuboid(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F)
                        .uv(40, 38).cuboid(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F),
                ModelTransform.of(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F)
        );
        arms.addChild("left_shoulder",
                ModelPartBuilder.create().uv(44, 22).mirrored().cuboid(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                ModelTransform.NONE
        );

        root.addChild(
                EntityModelPartNames.RIGHT_LEG,
                ModelPartBuilder.create().uv(0, 22).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(-2.0F, 12.0F, 0.0F)
        );

        root.addChild(
                EntityModelPartNames.LEFT_LEG,
                ModelPartBuilder.create().uv(0, 22).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(2.0F, 12.0F, 0.0F)
        );

        root.addChild(
                EntityModelPartNames.RIGHT_ARM,
                ModelPartBuilder.create().uv(40, 46).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(-5.0F, 2.0F, 0.0F)
        );

        root.addChild(
                EntityModelPartNames.LEFT_ARM,
                ModelPartBuilder.create().uv(40, 46).mirrored().cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(5.0F, 2.0F, 0.0F)
        );

        return TexturedModelData.of(modelData, 64, 64);
    }
}

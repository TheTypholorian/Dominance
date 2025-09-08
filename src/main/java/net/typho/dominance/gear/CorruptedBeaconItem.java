package net.typho.dominance.gear;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.typho.dominance.Dominance;
import net.typho.dominance.client.DamageParticleEffect;

import java.util.Optional;

public class CorruptedBeaconItem extends Item implements Equipment {
    public CorruptedBeaconItem(Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);

        if (!world.isClient) {
            Vec3d min = user.getPos();
            Vec3d max = min.add(user.getRotationVector().multiply(32f));
            Box box = new Box(min, max).expand(1);

            for (Entity target : world.getOtherEntities(user, box, Entity::isAttackable)) {
                Box box2 = target.getBoundingBox().expand(target.getTargetingMargin() + 1);
                Optional<Vec3d> optional = box2.raycast(min, max);

                if (box2.contains(min) || optional.isPresent()) {
                    target.damage(user.getDamageSources().create(DamageTypes.MAGIC, user), 7.5f);
                }
            }
        }

        Vec3d pos = user.getPos();
        Vec3d inc = user.getRotationVector().multiply(0.5f);

        for (float f = 0; f < 32; f += 0.5f) {
            world.addParticle(new DamageParticleEffect(Dominance.CORRUPTED_BEACON_PARTICLE, f), pos.x, pos.y, pos.z, 0, 0, 0);
            pos = pos.add(inc);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.MAINHAND;
    }
}

package net.typho.dominance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldEvents;

public class RoyalGuardMaceItem extends Item {
    public RoyalGuardMaceItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        DamageSource damageSource = attacker instanceof PlayerEntity player ? target.getWorld().getDamageSources().playerAttack(player) : target.getWorld().getDamageSources().mobAttack(attacker);
        int i = 0;

        for (Entity splash : target.getWorld().getOtherEntities(target, new Box(target.getBlockPos()).expand(2))) {
            if (splash != attacker && splash instanceof LivingEntity livingSplash) {
                livingSplash.damage(damageSource, (float) attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * MathHelper.clamp(1 - splash.distanceTo(target) / 7, 0, 0.5f));
                i++;
            }
        }

        if (target.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(
                    null, target.getX(), target.getY(), target.getZ(), i > 3 ? SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY : SoundEvents.ITEM_MACE_SMASH_GROUND, attacker.getSoundCategory(), 1, 1
            );
            serverWorld.syncWorldEvent(WorldEvents.SMASH_ATTACK, target.getSteppingPos(), 750);
        }

        return super.postHit(stack, target, attacker);
    }

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, EquipmentSlot.MAINHAND);
    }
}

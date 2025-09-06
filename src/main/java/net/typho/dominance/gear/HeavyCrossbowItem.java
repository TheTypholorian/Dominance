package net.typho.dominance.gear;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import org.jetbrains.annotations.Nullable;

public class HeavyCrossbowItem extends CrossbowItem {
    public HeavyCrossbowItem(Settings settings) {
        super(settings);
    }

    @Override
    protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target) {
        super.shoot(shooter, projectile, index, speed * 1.5f, divergence / 2, yaw, target);
    }
}

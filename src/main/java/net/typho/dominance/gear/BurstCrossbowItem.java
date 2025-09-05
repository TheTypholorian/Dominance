package net.typho.dominance.gear;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import org.jetbrains.annotations.Nullable;

public class BurstCrossbowItem extends CrossbowItem implements Salvageable {
    public BurstCrossbowItem(Settings settings) {
        super(settings);
    }

    public int extraProjectiles() {
        return 2;
    }

    @Override
    protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target) {
        if (index > 0 && index <= extraProjectiles()) {
            super.shoot(shooter, projectile, index, speed * (1 - index / 20f), 0, yaw, target);
        } else {
            super.shoot(shooter, projectile, index, speed, divergence, yaw, target);
        }
    }
}

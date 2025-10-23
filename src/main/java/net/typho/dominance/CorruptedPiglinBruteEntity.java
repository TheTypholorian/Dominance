package net.typho.dominance;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.world.World;

public class CorruptedPiglinBruteEntity extends PiglinBruteEntity {
    public CorruptedPiglinBruteEntity(EntityType<? extends CorruptedPiglinBruteEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 75)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8);
    }
}

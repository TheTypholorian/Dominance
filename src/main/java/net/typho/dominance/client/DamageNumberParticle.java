package net.typho.dominance.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DamageNumberParticle {
    public final Vec3d pos;
    public final float damage;
    public int age = 0;

    public static final List<DamageNumberParticle> PARTICLES = new LinkedList<>();

    public DamageNumberParticle(Vec3d pos, float damage) {
        this.pos = pos;
        this.damage = damage;
    }

    public void tick() {
        age++;

        if (age > 20) {
            PARTICLES.removeIf(particle -> particle == DamageNumberParticle.this);
        }
    }
}

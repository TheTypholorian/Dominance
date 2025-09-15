package net.typho.dominance;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public interface Interpolator {
    Interpolator LINEAR = (min, max, delta) -> new Cutscene.NodeInfo(
            max.pos().subtract(min.pos()).multiply(delta).add(min.pos()),
            (max.pitch() - min.pitch()) * delta + min.pitch(),
            (max.yaw() - min.yaw()) * delta + min.yaw()
    );

    Cutscene.NodeInfo interpolate(Cutscene.NodeInfo min, Cutscene.NodeInfo max, float delta);

    default Interpolator tweakDelta(FloatUnaryOperator op) {
        Interpolator og = this;
        return (min, max, delta) -> og.interpolate(min, max, op.apply(delta));
    }

    default Interpolator expDelta() {
        return tweakDelta(delta -> {
            float f = (delta - 0.5f) * 2;
            f *= f;
            return f / 2 + 0.5f;
        });
    }

    static Interpolator bezier(double x, double y, double z) {
        return bezier(new Vec3d(x, y, z));
    }

    static Interpolator bezier(Vec3d pivot) {
        return (min, max, delta) -> new Cutscene.NodeInfo(
                min.pos().lerp(pivot, delta).lerp(pivot.lerp(max.pos(), delta), delta),
                MathHelper.lerp(delta, min.pitch(), max.pitch()),
                MathHelper.lerp(delta, min.yaw(), max.yaw())
        );
    }
}

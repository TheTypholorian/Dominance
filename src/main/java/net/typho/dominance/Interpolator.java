package net.typho.dominance;

public interface Interpolator {
    Interpolator LINEAR = (min, max, delta) -> new Cutscene.NodeInfo(
            max.pos().subtract(min.pos()).multiply(delta).add(min.pos()),
            (max.pitch() - min.pitch()) * delta + min.pitch(),
            (max.yaw() - min.yaw()) * delta + min.yaw()
    );

    Cutscene.NodeInfo interpolate(Cutscene.NodeInfo min, Cutscene.NodeInfo max, float delta);
}

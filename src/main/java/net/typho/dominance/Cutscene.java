package net.typho.dominance;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Cutscene {
    public static Cutscene ACTIVE;

    public Node node;
    public NodeInfo info = new NodeInfo(new Vec3d(0, 0, 0), 0, 0);
    public float start, time;

    public <N extends Node> N start(N node) {
        start = (float) GLFW.glfwGetTime();
        time = 0;
        this.node = node;

        ACTIVE = this;

        return node;
    }

    public boolean update(Camera camera) {
        time = (float) GLFW.glfwGetTime() - start;

        if (!node.matchesTime(time)) {
            node = node.next();

            if (node == null) {
                if (ACTIVE == this) {
                    ACTIVE = null;
                }

                System.err.println("Finished cutscene");
                return false;
            }
        }

        info = node.get(time);
        camera.setPos(info.pos());
        camera.setRotation(info.yaw(), info.pitch());

        return true;
    }

    public interface Node {
        default boolean matchesTime(float time) {
            return minTime() <= time && maxTime() > time;
        }

        float minTime();

        float maxTime();

        NodeInfo get(float time);

        Node next();

        Node next(Node next);
    }

    public record NodeInfo(Vec3d pos, float pitch, float yaw) {
        public NodeInfo(double x, double y, double z, float pitch, float yaw) {
            this(new Vec3d(x, y, z), pitch, yaw);
        }
    }

    public static class InterpolationNode implements Node {
        public final Interpolator interpolator;
        public final NodeInfo min, max;
        public final float minTime, maxTime;
        public Node next;

        public InterpolationNode(Interpolator interpolator, NodeInfo min, NodeInfo max, float minTime, float maxTime) {
            this.interpolator = interpolator;
            this.min = min;
            this.max = max;
            this.minTime = minTime;
            this.maxTime = maxTime;
        }

        public InterpolationNode(Interpolator interpolator, NodeInfo min, NodeInfo max, float minTime, float maxTime, Node next) {
            this(interpolator, min, max, minTime, maxTime);
            this.next = next;
        }

        @Override
        public float minTime() {
            return minTime;
        }

        @Override
        public float maxTime() {
            return maxTime;
        }

        @Override
        public NodeInfo get(float time) {
            return interpolator.interpolate(min, max, (time - minTime) / (maxTime - minTime));
        }

        @Override
        public Node next() {
            return next;
        }

        @Override
        public Node next(Node next) {
            return this.next = next;
        }

        public InterpolationNode then(NodeInfo node, float duration) {
            return then(interpolator, node, duration);
        }

        public InterpolationNode then(Interpolator interpolator, NodeInfo node, float duration) {
            InterpolationNode n = new InterpolationNode(interpolator, max, node, maxTime, maxTime + duration);
            next = n;
            return n;
        }
    }

    public static class SnapNode implements Node {
        public final NodeInfo info;
        public final float minTime, maxTime;
        public Node next;

        public SnapNode(NodeInfo info, float minTime, float maxTime) {
            this.info = info;
            this.minTime = minTime;
            this.maxTime = maxTime;
        }

        public SnapNode(NodeInfo info, float minTime, float maxTime, Node next) {
            this(info, minTime, maxTime);
            this.next = next;
        }

        @Override
        public float minTime() {
            return minTime;
        }

        @Override
        public float maxTime() {
            return maxTime;
        }

        @Override
        public NodeInfo get(float time) {
            return info;
        }

        @Override
        public Node next() {
            return next;
        }

        @Override
        public Node next(Node next) {
            return this.next = next;
        }

        public SnapNode then(NodeInfo node, float duration) {
            SnapNode snap = new SnapNode(node, maxTime, maxTime + duration);
            next = snap;
            return snap;
        }
    }
}

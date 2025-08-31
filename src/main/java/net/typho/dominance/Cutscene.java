package net.typho.dominance;

import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class Cutscene {
    public static final List<Cutscene> ACTIVE = new LinkedList<>();

    public Node node;
    public NodeInfo info = new NodeInfo(new Vec3d(0, 0, 0), 0, 0);
    public float start, time;

    public void start(Node node) {
        start = (float) GLFW.glfwGetTime();
        time = 0;
        this.node = node;
        ACTIVE.add(this);
    }

    public void update() {
        time = (float) GLFW.glfwGetTime() - start;

        if (!node.matchesTime(time)) {
            node = node.next();

            if (node == null) {
                System.err.println("Somebody messed up a cutscene");
                return;
            }
        }

        info = node.get(time);
    }

    public static Cutscene current() {
        return ACTIVE.isEmpty() ? null : ACTIVE.getFirst();
    }

    public interface Node {
        boolean matchesTime(float time);

        NodeInfo get(float time);

        Node next();
    }

    public record NodeInfo(Vec3d pos, float pitch, float yaw) {
        public NodeInfo(double x, double y, double z, float pitch, float yaw) {
            this(new Vec3d(x, y, z), pitch, yaw);
        }
    }

    public record InterpolationNode(Interpolator interpolator, NodeInfo min, NodeInfo max, float minTime, float maxTime, Node next) implements Node {
        @Override
        public boolean matchesTime(float time) {
            return minTime <= time && maxTime > time;
        }

        @Override
        public NodeInfo get(float time) {
            return interpolator.interpolate(min, max, (time - minTime) / (maxTime - minTime));
        }
    }

    public record SnapNode(NodeInfo info, float minTime, float maxTime, Node next) implements Node {
        @Override
        public boolean matchesTime(float time) {
            return minTime <= time && maxTime > time;
        }

        @Override
        public NodeInfo get(float time) {
            return info;
        }
    }
}

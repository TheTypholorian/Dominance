package net.typho.dominance;

import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Cutscene {
    public static final List<Cutscene> ACTIVE = new LinkedList<>();

    public final List<Node> nodes = new LinkedList<>();
    public Vec3d pos = new Vec3d(0, 0, 0);
    public float pitch, yaw;
    public float start, time;

    public void start() {
        start = (float) GLFW.glfwGetTime();
        time = 0;
        ACTIVE.add(this);
    }

    public void update() {
        float end = nodes.getLast().time;

        if (time > end) {
            ACTIVE.removeIf(c -> c == Cutscene.this);
        } else {
            time = (float) GLFW.glfwGetTime() - start;
            Node first = null, second = null;
            Iterator<Node> firstIt = nodes.iterator(), secondIt = nodes.iterator();
            secondIt.next();

            while (secondIt.hasNext()) {
                first = firstIt.next();
                second = secondIt.next();

                if (first.time <= time && second.time > time) {
                    break;
                }
            }

            if (first == null || second == null) {
                ACTIVE.removeIf(c -> c == Cutscene.this);
                System.err.println("Somebody messed up a cutscene");
            } else {
                float delta = (time - first.time) / (second.time - first.time);
                pos = second.pos.subtract(first.pos).multiply(delta).add(first.pos);
                pitch = (second.pitch - first.pitch) * delta + first.pitch;
                yaw = (second.yaw - first.yaw) * delta + first.yaw;
            }
        }
    }

    public static Cutscene current() {
        return ACTIVE.isEmpty() ? null : ACTIVE.getFirst();
    }

    public record Node(Vec3d pos, float pitch, float yaw, float time) {
    }
}

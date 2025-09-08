package net.typho.dominance.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class CorruptedBeaconParticle extends Particle {
    public final CorruptedBeaconParticleEffect params;

    public CorruptedBeaconParticle(CorruptedBeaconParticleEffect params, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.velocityX /= 4;
        this.velocityY /= 4;
        this.velocityZ /= 4;
        this.params = params;
        maxAge *= 2;
    }

    @Override
    public void buildGeometry(VertexConsumer consumer, Camera camera, float tickDelta) {
        MatrixStack matrices = new MatrixStack();
        matrices.push();
        matrices.translate(
                x + velocityX * tickDelta - camera.getPos().x,
                y + velocityY * tickDelta - camera.getPos().y,
                z + velocityZ * tickDelta - camera.getPos().z
        );

        matrices.pop();
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }
}

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

import java.util.Objects;

public class DamageParticle extends Particle {
    public final DamageParticleEffect params;

    public DamageParticle(DamageParticleEffect params, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
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
                //this.velocityX + (this.x - this.velocityX) * tickDelta - camera.getPos().x,
                //this.velocityY + (this.y - this.velocityY) * tickDelta - camera.getPos().y,
                //this.velocityZ + (this.z - this.velocityZ) * tickDelta - camera.getPos().z
                x + velocityX * tickDelta - camera.getPos().x,
                y + velocityY * tickDelta - camera.getPos().y,
                z + velocityZ * tickDelta - camera.getPos().z
        );
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.scale(-0.02f, -0.02f, 0.02f);

        Text text = Text.literal(String.format("%.1f", params.damage));

        MinecraftClient.getInstance().textRenderer.draw(
                text,
                -MinecraftClient.getInstance().textRenderer.getWidth(text) / 2f,
                0,
                ((int) (MathHelper.clamp(1 - (float) age / maxAge, 0, 1) * 255) << 24) | Objects.requireNonNull(Formatting.RED.getColorValue()),
                false,
                matrices.peek().getPositionMatrix(),
                MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers(),
                TextRenderer.TextLayerType.NORMAL,
                Integer.MIN_VALUE,
                0xF000F0
        );

        matrices.pop();
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }
}

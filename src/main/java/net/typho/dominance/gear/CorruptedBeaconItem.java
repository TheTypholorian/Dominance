package net.typho.dominance.gear;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.vertex.VertexArray;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.typho.dominance.Dominance;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class CorruptedBeaconItem extends Item implements Equipment {
    public static VertexArray BEAM_MODEL;

    public CorruptedBeaconItem(Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    public Vec3d getUsePos(LivingEntity user, float delta) {
        return new Vec3d(
                MathHelper.lerp(delta, user.prevX, user.getX()),
                MathHelper.lerp(delta, user.prevY, user.getY()) + user.getStandingEyeHeight() * 0.5f,
                MathHelper.lerp(delta, user.prevZ, user.getZ())
        ).add(user.getRotationVec(delta).multiply(0.5));
    }

    public float maxLength() {
        return 128;
    }

    public void renderBeam(LivingEntity user, Camera camera, float tickDelta) {
        Vec3d pos = getUsePos(user, tickDelta);
        Matrix4fStack matrices = RenderSystem.getModelViewStack();
        HitResult raycast = user.raycast(maxLength(), tickDelta, false);
        matrices.pushMatrix();
        matrices.translate(
                (float) (pos.x - camera.getPos().x),
                (float) (pos.y - camera.getPos().y),
                (float) (pos.z - camera.getPos().z)
        );
        matrices.rotateY((float) Math.toRadians(-user.getYaw(tickDelta)));
        matrices.rotateX((float) Math.toRadians(user.getPitch(tickDelta)));
        matrices.scale(1, 1, raycast.getType() == HitResult.Type.MISS ? maxLength() : (float) raycast.getPos().distanceTo(pos));

        float pulse = (float) Math.sin(GLFW.glfwGetTime() * 8) * 0.75f + 2;

        BEAM_MODEL.bind();

        matrices.pushMatrix();
        matrices.rotateAround(RotationAxis.POSITIVE_Z.rotation((float) -GLFW.glfwGetTime() * 4), 0, 0.5f, 0);
        matrices.scaleAround(pulse / 2, pulse / 2, 1, 0, 0.5f, 0);
        RenderSystem.applyModelViewMatrix();
        BEAM_MODEL.drawWithRenderType(VeilRenderType.get(Dominance.id("corrupted_beacon_beam"), "back", Dominance.id("textures/item/corrupted_beacon_beam_inner.png")));
        matrices.popMatrix();

        matrices.pushMatrix();
        matrices.rotateAround(RotationAxis.POSITIVE_Z.rotation((float) GLFW.glfwGetTime() * 4), 0, 0.5f, 0);
        matrices.scaleAround(pulse, pulse, 1, 0, 0.5f, 0);
        RenderSystem.applyModelViewMatrix();
        BEAM_MODEL.drawWithRenderType(VeilRenderType.get(Dominance.id("corrupted_beacon_beam"), "front", Dominance.id("textures/item/corrupted_beacon_beam_outer.png")));
        matrices.popMatrix();

        matrices.popMatrix();
    }

    public void renderBeamEnd(PlayerEntity user) {
        HitResult raycast = user.raycast(maxLength(), 1f, false);
        int num = switch (MinecraftClient.getInstance().options.getParticles().getValue()) {
            case ALL -> 5;
            case DECREASED -> 2;
            case MINIMAL -> 1;
        };

        for (int i = 0; i < num; i++) {
            Vec3d pos = raycast.getPos().add(
                    user.getRandom().nextFloat() * 0.5 - 0.25,
                    user.getRandom().nextFloat() * 0.5 - 0.25,
                    user.getRandom().nextFloat() * 0.5 - 0.25
            );
            user.getWorld().addImportantParticle(ParticleTypes.DRAGON_BREATH, pos.x, pos.y, pos.z, 0, 0, 0);
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);

        if (!world.isClient) {
            Vec3d min = getUsePos(user, 1f);
            Vec3d max = user.raycast(maxLength(), 1f, false).getPos();
            Box box = new Box(min, max).expand(1);

            for (Entity target : world.getOtherEntities(user, box, Entity::isAttackable)) {
                Box box2 = target.getBoundingBox().expand(target.getTargetingMargin() + 1);
                Optional<Vec3d> optional = box2.raycast(min, max);

                if (box2.contains(min) || optional.isPresent()) {
                    target.damage(user.getDamageSources().create(DamageTypes.MAGIC, user), 7.5f);
                }
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.MAINHAND;
    }
}

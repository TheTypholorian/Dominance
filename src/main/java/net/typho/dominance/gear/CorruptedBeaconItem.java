package net.typho.dominance.gear;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.typho.dominance.Dominance;
import org.joml.Matrix4fStack;

import java.util.Optional;
import java.util.function.BiConsumer;

public class CorruptedBeaconItem extends Item implements Equipment {
    public static BiConsumer<VertexConsumer, Integer> BEAM_MODEL;

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
                MathHelper.lerp(delta, user.prevY, user.getY()),
                MathHelper.lerp(delta, user.prevZ, user.getZ())
        );
    }

    public void renderBeam(LivingEntity user, VertexConsumerProvider consumers, Camera camera, float tickDelta) {
        Vec3d pos = getUsePos(user, tickDelta);
        Matrix4fStack matrices = RenderSystem.getModelViewStack();
        matrices.pushMatrix();
        matrices.translate(
                (float) (pos.x - camera.getPos().x),
                (float) (pos.y - camera.getPos().y),
                (float) (pos.z - camera.getPos().z)
        );
        matrices.rotateY((float) Math.toRadians(-user.getYaw(tickDelta)));
        matrices.rotateX((float) Math.toRadians(user.getPitch(tickDelta)));
        matrices.scale(1, 1, 32);
        RenderSystem.applyModelViewMatrix();
        BEAM_MODEL.accept(consumers.getBuffer(VeilRenderType.get(Dominance.id("corrupted_beacon_beam"), "back")), LightmapTextureManager.MAX_LIGHT_COORDINATE);

        matrices.scale(1.25f, 1.25f, 1);
        RenderSystem.applyModelViewMatrix();
        BEAM_MODEL.accept(consumers.getBuffer(VeilRenderType.get(Dominance.id("corrupted_beacon_beam"), "front")), LightmapTextureManager.MAX_LIGHT_COORDINATE);

        matrices.popMatrix();
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);

        if (!world.isClient) {
            Vec3d min = getUsePos(user, 1f);
            Vec3d max = min.add(user.getRotationVector().multiply(32f));
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

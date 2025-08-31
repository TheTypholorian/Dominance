package net.typho.dominance.mixin.client;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.typho.dominance.Cutscene;
import net.typho.dominance.client.DominanceClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPos(Vec3d pos);

    @Inject(
            method = "update",
            at = @At("HEAD"),
            cancellable = true
    )
    private void update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (DominanceClient.TEST_CUTSCENE.isPressed() && Cutscene.ACTIVE.isEmpty()) {
            System.out.println("Implement test cutscene");
            /*
            Cutscene cutscene = new Cutscene();

            cutscene.nodes.add(new Cutscene.SnapNode(new Vec3d(0, 0, 0), 0, 0, 0));
            cutscene.nodes.add(new Cutscene.SnapNode(new Vec3d(0, 15, 0), 0, 0, 2));
            cutscene.nodes.add(new Cutscene.SnapNode(new Vec3d(18, 13, 0), 35, 0, 3));
            cutscene.nodes.add(new Cutscene.SnapNode(new Vec3d(18, 13, 7), 30, 50, 5));
            cutscene.nodes.add(new Cutscene.SnapNode(new Vec3d(18, 13, 7), 30, 360, 10));

            new Cutscene().start(
                    new Cutscene.InterpolationNode(
                            Interpolator.LINEAR,
                            new Cutscene.NodeInfo(0, 0, 0, 0, 0),
                            new Cutscene.NodeInfo(0, 0, 15, 0, 0),
                            0,
                            2,
                            new Cutscene.InterpolationNode(
                                    Interpolator.LINEAR
                            )
                    )
            );
             */
        }

        Cutscene.ACTIVE.forEach(Cutscene::update);

        Cutscene cutscene = Cutscene.current();

        if (cutscene != null) {
            setPos(cutscene.info.pos());
            setRotation(cutscene.info.yaw(), cutscene.info.pitch());
            ci.cancel();
        }
    }
}

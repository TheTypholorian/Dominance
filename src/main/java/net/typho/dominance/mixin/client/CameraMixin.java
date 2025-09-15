package net.typho.dominance.mixin.client;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.typho.dominance.Cutscene;
import net.typho.dominance.Interpolator;
import net.typho.dominance.client.DominanceClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPos(Vec3d pos);

    @Inject(
            method = "isThirdPerson",
            at = @At("HEAD"),
            cancellable = true
    )
    private void isThirdPerson(CallbackInfoReturnable<Boolean> cir) {
        if (Cutscene.ACTIVE != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "update",
            at = @At("HEAD"),
            cancellable = true
    )
    private void update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (DominanceClient.TEST_CUTSCENE.isPressed() && Cutscene.ACTIVE == null) {
            System.out.println("Implement test cutscene");
            new Cutscene().start(
                            new Cutscene.InterpolationNode(
                                    Interpolator.bezier(-15, 0, 0),
                                    new Cutscene.NodeInfo(15, 0, 0, -90, 0),
                                    new Cutscene.NodeInfo(-15, 15, 0, 0, 0),
                                    0,
                                    5
                            )
                    )
                    .then(Interpolator.bezier(18, 13, 5), new Cutscene.NodeInfo(18, 13, 0, 35, 0), 1)
                    .then(Interpolator.bezier(15, 13, 7), new Cutscene.NodeInfo(18, 13, 7, 30, 50), 2)
                    .then(new Cutscene.NodeInfo(18, 13, 7, 30, 360), 5);
        }

        if (Cutscene.ACTIVE != null) {
            if (Cutscene.ACTIVE.update((Camera) (Object) this)) {
                ci.cancel();
            }
        }
    }
}

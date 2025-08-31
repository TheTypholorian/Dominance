package net.typho.dominance.mixin.client;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.typho.dominance.Dominance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {
    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        if (stack.isOf(Dominance.ROYAL_GUARD_SHIELD)) {
            matrices.push();
            matrices.scale(1.0F, -1.0F, -1.0F);
            SpriteIdentifier spriteIdentifier = Dominance.ROYAL_GUARD_SHIELD_SPRITE;
            VertexConsumer vertexConsumer = spriteIdentifier.getSprite()
                    .getTextureSpecificVertexConsumer(
                            ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, Dominance.ROYAL_GUARD_SHIELD_MODEL.getLayer(spriteIdentifier.getAtlasId()), true, stack.hasGlint())
                    );
            Dominance.ROYAL_GUARD_SHIELD_MODEL.getHandle().render(matrices, vertexConsumer, light, overlay);
            Dominance.ROYAL_GUARD_SHIELD_MODEL.getPlate().render(matrices, vertexConsumer, light, overlay);
            matrices.pop();
            ci.cancel();
        }
    }
}

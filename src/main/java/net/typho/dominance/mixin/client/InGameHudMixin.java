package net.typho.dominance.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.typho.dominance.Cutscene;
import net.typho.dominance.Dominance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private static Identifier ARMOR_FULL_TEXTURE;

    @Shadow
    @Final
    private static Identifier ARMOR_HALF_TEXTURE;

    @Shadow
    @Final
    private static Identifier ARMOR_EMPTY_TEXTURE;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(
            method = "renderExperienceBar",
            at = @At("HEAD")
    )
    private void renderExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        int souls = Dominance.PLAYER_DATA.get(client.player).getSouls();
        int k = (int) ((float) souls / client.player.getAttributeValue(Dominance.PLAYER_MAX_SOULS) * 183.0F);
        int l = context.getScaledWindowHeight() - 29;

        RenderSystem.enableBlend();
        context.drawGuiTexture(Dominance.id("hud/soul_bar_background"), x, l, 182, 7);

        if (k > 0) {
            context.drawGuiTexture(Dominance.id("hud/soul_bar_progress"), 182, 7, 0, 0, x, l, k, 7);
        }

        RenderSystem.disableBlend();
    }

    @WrapOperation(
            method = {
                    "renderExperienceBar",
                    "renderStatusBars",
                    "renderExperienceLevel"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;getScaledWindowHeight()I"
            )
    )
    private int getScaledWindowHeight(DrawContext instance, Operation<Integer> original) {
        return original.call(instance) - 5;
    }

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Cutscene.ACTIVE != null) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderArmor",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void renderArmor(DrawContext context, PlayerEntity player, int i, int j, int k, int x, CallbackInfo ci) {
        int l = player.getArmor();

        if (l > 0) {
            RenderSystem.enableBlend();
            int m = i - (j - 1) * k - 10;
            int o = x;

            for (int n = 0; n < l; n += 2, o += 8) {
                if (n + 1 < l) {
                    context.drawGuiTexture(ARMOR_FULL_TEXTURE, o, m, 9, 9);
                }

                if (n + 1 == l) {
                    context.drawGuiTexture(ARMOR_HALF_TEXTURE, o, m, 9, 9);
                }

                if (n + 1 > l) {
                    context.drawGuiTexture(ARMOR_EMPTY_TEXTURE, o, m, 9, 9);
                }

                if (n != 0 && n % 18 == 0) {
                    o = x - 8;
                    m -= 10;
                }
            }

            RenderSystem.disableBlend();
        }

        ci.cancel();
    }
}

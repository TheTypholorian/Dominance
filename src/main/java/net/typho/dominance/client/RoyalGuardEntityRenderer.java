package net.typho.dominance.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.typho.dominance.Dominance;
import net.typho.dominance.RoyalGuardEntity;

public class RoyalGuardEntityRenderer extends BipedIllagerEntityRenderer<RoyalGuardEntity> {
    private static final Identifier TEXTURE = Dominance.id("textures/entity/illager/royal_guard.png");

    public RoyalGuardEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, DominanceClient.ROYAL_GUARD_LAYER);
    }

    @Override
    public Identifier getTexture(RoyalGuardEntity entity) {
        return TEXTURE;
    }
}

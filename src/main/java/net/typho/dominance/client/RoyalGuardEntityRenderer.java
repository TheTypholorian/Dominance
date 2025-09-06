package net.typho.dominance.client;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;
import net.typho.dominance.Dominance;
import net.typho.dominance.RoyalGuardEntity;

public class RoyalGuardEntityRenderer extends BipedEntityRenderer<RoyalGuardEntity, BipedEntityModel<RoyalGuardEntity>> {
    private static final Identifier TEXTURE = Dominance.id("textures/entity/illager/royal_guard.png");

    protected RoyalGuardEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(DominanceClient.ROYAL_GUARD_LAYER)), 0.5F);
        addFeature(new ArmorFeatureRenderer<>(
                this,
                new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)),
                new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)),
                ctx.getModelManager()
        ));
    }

    @Override
    public Identifier getTexture(RoyalGuardEntity entity) {
        return TEXTURE;
    }
}

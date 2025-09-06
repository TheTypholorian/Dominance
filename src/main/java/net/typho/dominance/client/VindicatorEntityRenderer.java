package net.typho.dominance.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.util.Identifier;

public class VindicatorEntityRenderer extends BipedIllagerEntityRenderer<VindicatorEntity> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/illager/vindicator.png");

    public VindicatorEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, EntityModelLayers.VINDICATOR);
    }

    @Override
    public Identifier getTexture(VindicatorEntity entity) {
        return TEXTURE;
    }
}

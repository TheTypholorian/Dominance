package net.typho.dominance.client;

import net.minecraft.util.Identifier;
import net.typho.dominance.Dominance;
import net.typho.dominance.RoyalGuardArmorItem;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class RoyalGuardArmorRenderer extends GeoArmorRenderer<RoyalGuardArmorItem> {
    public RoyalGuardArmorRenderer(String path) {
        super(new DefaultedItemGeoModel<>(Identifier.of(Dominance.MOD_ID, "armor/royal_guard_" + path)));
    }
}

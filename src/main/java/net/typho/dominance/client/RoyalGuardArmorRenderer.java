package net.typho.dominance.client;

import net.typho.dominance.Dominance;
import net.typho.dominance.gear.RoyalGuardArmorItem;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class RoyalGuardArmorRenderer extends GeoArmorRenderer<RoyalGuardArmorItem> {
    public RoyalGuardArmorRenderer(String path) {
        super(new DefaultedItemGeoModel<>(Dominance.id("armor/royal_guard_" + path)));
    }
}

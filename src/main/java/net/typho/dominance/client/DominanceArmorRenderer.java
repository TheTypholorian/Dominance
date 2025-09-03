package net.typho.dominance.client;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.typho.dominance.gear.RoyalGuardArmorItem;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DominanceArmorRenderer extends GeoArmorRenderer<RoyalGuardArmorItem> {
    public DominanceArmorRenderer(Identifier id) {
        super(new DefaultedItemGeoModel<>(id.withPrefixedPath("armor/")));
    }

    public DominanceArmorRenderer(Item item) {
        this(Registries.ITEM.getId(item));
    }
}

package net.typho.dominance.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.component.ComponentHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.typho.dominance.Dominance;
import net.typho.dominance.gear.Reforge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {
    @ModifyReturnValue(
            method = "getName",
            at = @At("RETURN")
    )
    private Text getName(Text name) {
        Reforge reforge = get(Dominance.REFORGE_COMPONENT);

        if (reforge != null) {
            return reforge.name((ItemStack) (Object) this, name);
        }

        return name;
    }
}

package net.typho.dominance.gear;

import net.minecraft.item.ItemStack;
import net.typho.dominance.Dominance;

import java.util.Random;

public interface Salvageable {
    default ItemStack salvage(ItemStack stack) {
        Random random = new Random();
        int count = switch (stack.getRarity()) {
            case COMMON, UNCOMMON -> random.nextInt(0, 2);
            case RARE -> random.nextInt(1, 3);
            case EPIC -> random.nextInt(2, 4);
        };
        return count == 0 ? ItemStack.EMPTY : new ItemStack(Dominance.REFORGE_SMITHING_TEMPLATE, count);
    }
}

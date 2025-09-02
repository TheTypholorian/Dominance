package net.typho.dominance.enchants;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public record DamageModifier(float value, Operation op) {
    public void accept(MutableFloat f) {
        op.accept(value, f);
    }

    @Override
    public @NotNull String toString() {
        return op + " " + value;
    }

    public enum Operation {
        ADD(Float::sum),
        SUBTRACT((a, b) -> a - b),
        MULTIPLY((a, b) -> a * b),
        DIVIDE((a, b) -> a / b),
        POWER((a, b) -> (float) Math.pow(a, b));

        private final BiFunction<Float, Float, Float> func;

        Operation(BiFunction<Float, Float, Float> func) {
            this.func = func;
        }

        public void accept(float in, MutableFloat out) {
            out.setValue(func.apply(out.getValue(), in));
        }
    }
}

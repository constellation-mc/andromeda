package me.melontini.andromeda.util.commander;

import net.minecraft.loot.context.LootContext;

import java.util.function.Supplier;

public interface NumberIntermediary {

    double asDouble(Supplier<LootContext> supplier);

    default long asLong(Supplier<LootContext> supplier) {
        return (long) this.asDouble(supplier);
    }

    default int asInt(Supplier<LootContext> supplier) {
        return (int) this.asDouble(supplier);
    }

    default float asFloat(Supplier<LootContext> supplier) {
        return (float) this.asDouble(supplier);
    }

    static NumberIntermediary of(double value) {
        return new ConstantNumberIntermediary(value);
    }
}

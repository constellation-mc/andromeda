package me.melontini.andromeda.util.commander;

import me.melontini.commander.api.expression.Arithmetica;
import me.melontini.dark_matter.api.base.util.Support;
import net.minecraft.loot.context.LootContext;

import java.util.function.DoubleFunction;
import java.util.function.Supplier;

//We swap out this common interface to one of its impls. This allows us to support running with and without commander.
//Backends cannot be mixed as doing so will result in a ClassCastException.
public interface NumberIntermediary {

    DoubleFunction<NumberIntermediary> FACTORY = Support.support("commander",
            () -> d -> new CommanderNumberIntermediary(Arithmetica.constant(d)),
            () -> ConstantNumberIntermediary::new);

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
        return FACTORY.apply(value);
    }
}

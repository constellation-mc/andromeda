package me.melontini.andromeda.util.commander.number;

import me.melontini.andromeda.util.commander.number.constant.ConstantDoubleIntermediary;
import me.melontini.andromeda.util.commander.number.expression.CommanderDoubleIntermediary;
import me.melontini.commander.api.expression.Arithmetica;
import me.melontini.dark_matter.api.base.util.Support;
import net.minecraft.loot.context.LootContext;

import java.util.function.DoubleFunction;
import java.util.function.Supplier;

//We swap out this common interface to one of its impls. This allows us to support running with and without commander.
//Backends cannot be mixed as doing so will result in a ClassCastException.
public interface DoubleIntermediary {

    DoubleFunction<DoubleIntermediary> FACTORY = Support.support("commander",
            () -> d -> new CommanderDoubleIntermediary(Arithmetica.constant(d)),
            () -> ConstantDoubleIntermediary::new);

    double asDouble(Supplier<LootContext> supplier);

    default float asFloat(Supplier<LootContext> supplier) {
        return (float) this.asDouble(supplier);
    }

    static DoubleIntermediary of(double value) {
        return FACTORY.apply(value);
    }
}

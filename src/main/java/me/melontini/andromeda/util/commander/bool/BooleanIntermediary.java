package me.melontini.andromeda.util.commander.bool;

import me.melontini.commander.api.expression.BooleanExpression;
import me.melontini.dark_matter.api.base.util.Support;
import net.fabricmc.fabric.api.util.BooleanFunction;
import net.minecraft.loot.context.LootContext;

import java.util.function.Supplier;

public interface BooleanIntermediary {
    BooleanFunction<BooleanIntermediary> FACTORY = Support.support("commander",
            () -> b -> new CommanderBooleanIntermediary(BooleanExpression.constant(b)),
            () -> ConstantBooleanIntermediary::of);

    boolean asBoolean(Supplier<LootContext> supplier);

    static BooleanIntermediary of(boolean value) {
        return FACTORY.apply(value);
    }
}

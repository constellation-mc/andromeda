package me.melontini.andromeda.util.commander.number;

import java.util.function.LongFunction;
import java.util.function.Supplier;
import me.melontini.andromeda.util.commander.LongExpression;
import me.melontini.andromeda.util.commander.number.constant.ConstantLongIntermediary;
import me.melontini.andromeda.util.commander.number.expression.CommanderLongIntermediary;
import me.melontini.dark_matter.api.base.util.Support;
import net.minecraft.loot.context.LootContext;

public interface LongIntermediary {

  LongFunction<LongIntermediary> FACTORY = Support.support(
      "commander",
      () -> j -> new CommanderLongIntermediary(LongExpression.constant(j)),
      () -> ConstantLongIntermediary::new);

  long asLong(Supplier<LootContext> supplier);

  default int asInt(Supplier<LootContext> supplier) {
    return (int) this.asLong(supplier);
  }

  static LongIntermediary of(long value) {
    return FACTORY.apply(value);
  }
}

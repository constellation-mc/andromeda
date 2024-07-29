package me.melontini.andromeda.util.commander;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.ToLongFunction;
import me.melontini.commander.api.expression.Expression;
import me.melontini.dark_matter.api.data.codecs.ExtraCodecs;
import net.minecraft.loot.context.LootContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface LongExpression extends ToLongFunction<LootContext> {

  Codec<LongExpression> CODEC = ExtraCodecs.either(Codec.LONG, Codec.STRING)
      .comapFlatMap(
          (either) -> either.map(b -> DataResult.success(constant(b)), s -> Expression.parse(s)
              .map(LongExpression::of)),
          LongExpression::toSource);

  default long asLong(LootContext context) {
    return this.applyAsLong(context);
  }

  Either<Long, String> toSource();

  @Contract("_ -> new")
  static @NotNull LongExpression constant(long j) {
    Either<Long, String> either = Either.left(j);
    return new LongExpression() {
      @Override
      public Either<Long, String> toSource() {
        return either;
      }

      @Override
      public long applyAsLong(LootContext context) {
        return j;
      }

      @Override
      public String toString() {
        return "LongExpression{long=" + j + "}";
      }
    };
  }

  static @NotNull LongExpression of(Expression expression) {
    Either<Long, String> either = Either.right(expression.original());
    return new LongExpression() {
      @Override
      public Either<Long, String> toSource() {
        return either;
      }

      @Override
      public long applyAsLong(LootContext context) {
        return expression.apply(context).getAsDecimal().longValue();
      }

      @Override
      public String toString() {
        return "LongExpression{expression=" + expression + "}";
      }
    };
  }
}

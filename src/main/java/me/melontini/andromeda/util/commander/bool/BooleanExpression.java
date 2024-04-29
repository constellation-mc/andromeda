package me.melontini.andromeda.util.commander.bool;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import me.melontini.commander.api.expression.Expression;
import me.melontini.dark_matter.api.data.codecs.ExtraCodecs;
import net.minecraft.loot.context.LootContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

//TODO should be part of commander.
public interface BooleanExpression {

    Codec<BooleanExpression> CODEC = ExtraCodecs.either(Codec.BOOL, Codec.STRING).comapFlatMap((either) -> either.map(b -> DataResult.success(constant(b)), s -> Expression.parse(s).map(BooleanExpression::of)), BooleanExpression::toSource);

    boolean applyAsBoolean(LootContext context);

    Either<Boolean, String> toSource();

    @Contract("_ -> new")
    static @NotNull BooleanExpression constant(boolean d) {
        Either<Boolean, String> either = Either.left(d);
        return new BooleanExpression() {
            @Override
            public Either<Boolean, String> toSource() {
                return either;
            }

            @Override
            public boolean applyAsBoolean(LootContext context) {
                return d;
            }
        };
    }

    @Contract("_ -> new")
    static @NotNull BooleanExpression of(Expression expression) {
        Either<Boolean, String> either = Either.right(expression.original());
        return new BooleanExpression() {
            @Override
            public Either<Boolean, String> toSource() {
                return either;
            }

            @Override
            public boolean applyAsBoolean(LootContext context) {
                return expression.eval(context).getAsBoolean();
            }
        };
    }
}

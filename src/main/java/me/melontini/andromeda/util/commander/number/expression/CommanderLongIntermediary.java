package me.melontini.andromeda.util.commander.number.expression;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.ToString;
import me.melontini.andromeda.util.commander.LongExpression;
import me.melontini.andromeda.util.commander.number.LongIntermediary;
import net.minecraft.loot.context.LootContext;

import java.util.function.Supplier;

@ToString
public final class CommanderLongIntermediary implements LongIntermediary {

    public static final Codec<CommanderLongIntermediary> CODEC = LongExpression.CODEC.xmap(CommanderLongIntermediary::new, CommanderLongIntermediary::getArithmetica);

    @Getter
    private final LongExpression arithmetica;
    private final boolean constant;

    public CommanderLongIntermediary(LongExpression arithmetica) {
        this.arithmetica = arithmetica;
        this.constant = arithmetica.toSource().left().isPresent();
    }

    @Override
    public long asLong(Supplier<LootContext> supplier) {
        return this.arithmetica.asLong(constant ? null : supplier.get());
    }
}

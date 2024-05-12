package me.melontini.andromeda.util.commander.number.expression;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.ToString;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;
import me.melontini.commander.api.expression.Arithmetica;
import net.minecraft.loot.context.LootContext;

import java.util.function.Supplier;

@ToString
public final class CommanderDoubleIntermediary implements DoubleIntermediary {

    public static final Codec<CommanderDoubleIntermediary> CODEC = Arithmetica.CODEC.xmap(CommanderDoubleIntermediary::new, CommanderDoubleIntermediary::getArithmetica);

    @Getter
    private final Arithmetica arithmetica;
    private final boolean constant;

    public CommanderDoubleIntermediary(Arithmetica arithmetica) {
        this.arithmetica = arithmetica;
        this.constant = arithmetica.toSource().left().isPresent();
    }

    @Override
    public double asDouble(Supplier<LootContext> supplier) {
        return this.arithmetica.asDouble(constant ? null : supplier.get());
    }
}

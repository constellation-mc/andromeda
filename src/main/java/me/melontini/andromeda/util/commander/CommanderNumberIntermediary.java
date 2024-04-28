package me.melontini.andromeda.util.commander;

import com.mojang.serialization.Codec;
import lombok.Getter;
import me.melontini.commander.api.expression.Arithmetica;
import net.minecraft.loot.context.LootContext;

import java.util.function.Supplier;

public class CommanderNumberIntermediary implements NumberIntermediary {

    public static final Codec<CommanderNumberIntermediary> CODEC = Arithmetica.CODEC.xmap(CommanderNumberIntermediary::new, CommanderNumberIntermediary::getArithmetica);

    @Getter
    private final Arithmetica arithmetica;
    private final boolean constant;

    public CommanderNumberIntermediary(Arithmetica arithmetica) {
        this.arithmetica = arithmetica;
        this.constant = arithmetica.toSource().left().isPresent();
    }

    @Override
    public double asDouble(Supplier<LootContext> supplier) {
        return this.arithmetica.asDouble(constant ? null : supplier.get());
    }
}

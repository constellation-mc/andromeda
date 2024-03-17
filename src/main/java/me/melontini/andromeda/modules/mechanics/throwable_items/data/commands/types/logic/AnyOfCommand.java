package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import me.melontini.dark_matter.api.minecraft.data.ExtraCodecs;
import net.minecraft.loot.condition.LootCondition;

import java.util.List;
import java.util.Optional;

@Getter
@Accessors(fluent = true)
public class AnyOfCommand extends Command {

    public static final Codec<AnyOfCommand> CODEC = RecordCodecBuilder.create(data -> data.group(
            ExtraCodecs.list(CommandType.DISPATCH).fieldOf("commands").forGetter(AnyOfCommand::commands),
            ExtraCodecs.list(CommandType.DISPATCH).fieldOf("then").forGetter(AnyOfCommand::thenCommands),
            MiscUtil.LOOT_CONDITION_CODEC.optionalFieldOf("condition").forGetter(Command::getCondition)
    ).apply(data, AnyOfCommand::new));

    private final List<Command> commands;
    private final List<Command> thenCommands;

    public AnyOfCommand(List<Command> commands, List<Command> thenCommands, Optional<LootCondition> condition) {
        super(condition);
        this.commands = commands;
        this.thenCommands = thenCommands;
    }

    @Override
    public boolean execute(Context context) {
        if (!this.condition.map(condition1 -> condition1.test(context.lootContext())).orElse(true)) return false;

        boolean b = false;
        for (Command command : commands) {
            b |= command.tryExecute(context);
        }
        if (b) {
            b = false;
            for (Command then : thenCommands) {
                b |= then.tryExecute(context);
            }
            return b;
        }
        return false;
    }

    @Override
    public CommandType type() {
        return CommandType.ANY_OF;
    }
}

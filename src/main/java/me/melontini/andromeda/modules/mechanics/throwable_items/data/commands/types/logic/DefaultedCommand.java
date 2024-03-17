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

@Getter @Accessors(fluent = true)
public class DefaultedCommand extends Command {

    public static final Codec<DefaultedCommand> CODEC = RecordCodecBuilder.create(data -> data.group(
            ExtraCodecs.list(CommandType.DISPATCH).fieldOf("commands").forGetter(DefaultedCommand::commands),
            ExtraCodecs.list(CommandType.DISPATCH).fieldOf("default").forGetter(DefaultedCommand::defaultCommands),
            MiscUtil.LOOT_CONDITION_CODEC.optionalFieldOf("condition").forGetter(Command::getCondition)
    ).apply(data, DefaultedCommand::new));

    private final List<Command> commands;
    private final List<Command> defaultCommands;

    public DefaultedCommand(List<Command> commands, List<Command> defaultCommands, Optional<LootCondition> condition) {
        super(condition);
        this.commands = commands;
        this.defaultCommands = defaultCommands;
    }

    @Override
    public boolean execute(Context context) {
        if (!this.condition.map(condition1 -> condition1.test(context.lootContext())).orElse(true)) return false;

        boolean b = false;
        for (Command command : commands) {
            b |= command.tryExecute(context);
        }
        if (!b) {
            for (Command defaultCommand : defaultCommands) {
                b |= defaultCommand.tryExecute(context);
            }
            return b;
        }
        return true;
    }

    @Override
    public CommandType type() {
        return CommandType.DEFAULTED;
    }
}

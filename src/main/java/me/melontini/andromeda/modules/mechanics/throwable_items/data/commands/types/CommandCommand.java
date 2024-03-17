package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Selector;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.minecraft.data.ExtraCodecs;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.Optional;

@Getter
@Accessors(fluent = true)
public class CommandCommand extends Command {

    public static final Codec<CommandCommand> CODEC = RecordCodecBuilder.create(data -> data.group(
            Selector.CODEC.fieldOf("selector").forGetter(CommandCommand::selector),
            ExtraCodecs.list(Codec.STRING).fieldOf("commands").forGetter(CommandCommand::commands),
            MiscUtil.LOOT_CONDITION_CODEC.optionalFieldOf("condition").forGetter(Command::getCondition)
    ).apply(data, CommandCommand::new));

    protected final Selector selector;
    protected final List<String> commands;

    public CommandCommand(Selector selector, List<String> commands, Optional<LootCondition> condition) {
        super(condition);
        this.selector = selector;
        this.commands = commands;
    }

    @Override
    protected boolean execute(Context context) {
        ServerCommandSource source = selector.function().apply(context);
        if (source == null) return false;
        if (!Debug.Keys.PRINT_DATA_COMMAND_OUTPUT.isPresent()) source = source.withSilent();

        if (commands != null && !commands.isEmpty()) {
            for (String command : commands) {
                context.world().getServer().getCommandManager().executeWithPrefix(source, command);
            }
        }
        return true;
    }

    @Override
    public CommandType type() {
        return CommandType.COMMANDS;
    }
}

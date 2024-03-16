package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@Accessors(fluent = true)
public class AllOfCommand extends Command {

    public static final Codec<AllOfCommand> CODEC = RecordCodecBuilder.create(data -> data.group(
            MiscUtil.listCodec(CommandType.DISPATCH).fieldOf("commands").forGetter(AllOfCommand::commands),
            MiscUtil.listCodec(CommandType.DISPATCH).fieldOf("then").forGetter(AllOfCommand::thenCommands),
            MiscUtil.LOOT_CONDITION_CODEC.optionalFieldOf("condition").forGetter(Command::getCondition)
    ).apply(data, AllOfCommand::new));

    private final List<Command> commands;
    private final List<Command> thenCommands;

    public AllOfCommand(List<Command> commands, List<Command> thenCommands, Optional<LootCondition> condition) {
        super(Collections.emptyList(), ItemBehaviorData.Particles.EMPTY, condition);
        this.commands = commands;
        this.thenCommands = thenCommands;
    }

    @Override
    public boolean execute(Context context) {
        if (!this.condition.map(condition1 -> condition1.test(context.lootContext())).orElse(true)) return false;

        boolean b = true;
        for (Command command : commands) {
            b &= command.execute(context);
        }
        if (b) {
            b = false;
            for (Command then : thenCommands) {
                b |= then.execute(context);
            }
            return b;
        }
        return false;
    }

    @Override
    protected @Nullable ServerCommandSource createSource(Context context) {
        return null;
    }

    @Override
    public CommandType type() {
        return CommandType.ALL_OF;
    }
}

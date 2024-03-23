package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ServerCommand extends Command {
    public ServerCommand(List<String> commands, ItemBehaviorData.Particles particles, Optional<LootCondition> condition) {
        super(commands, particles, condition);
    }

    @Override
    @Nullable
    protected ServerCommandSource createSource(Context context) {
        return context.world().getServer().getCommandSource().withSilent();
    }

    @Override
    public CommandType type() {
        return CommandType.SERVER;
    }
}

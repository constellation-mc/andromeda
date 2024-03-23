package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class UserCommand extends Command {
    public UserCommand(List<String> commands, ItemBehaviorData.Particles particles, Optional<LootCondition> condition) {
        super(commands, particles, condition);
    }

    @Override
    @Nullable
    protected ServerCommandSource createSource(Context context) {
        var world = context.world(); var user = context.user();
        if (user == null) return null;

        return new ServerCommandSource(world.getServer(), user.getPos(),
                new Vec2f(user.getPitch(), user.getYaw()),
                world, 4, user.getEntityName(), user.getName(),
                world.getServer(), user);
    }

    @Override
    public CommandType type() {
        return CommandType.USER;
    }
}

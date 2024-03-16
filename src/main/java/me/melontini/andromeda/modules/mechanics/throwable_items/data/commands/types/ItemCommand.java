package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec2f;

import java.util.List;
import java.util.Optional;

public class ItemCommand extends Command {
    public ItemCommand(List<String> commands, ItemBehaviorData.Particles particles, Optional<LootCondition> condition) {
        super(commands, particles, condition);
    }

    @Override
    protected ServerCommandSource createSource(Context context) {
        var world = context.world(); var fie = context.fie();
        return new ServerCommandSource(world.getServer(), fie.getPos(),
                new Vec2f(fie.getPitch(), fie.getYaw()),
                world, 4, fie.getEntityName(), fie.getName(),
                world.getServer(), fie).withSilent();
    }

    @Override
    public CommandType type() {
        return CommandType.ITEM;
    }
}

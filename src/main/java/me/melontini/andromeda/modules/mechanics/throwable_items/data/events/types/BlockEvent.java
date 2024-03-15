package me.melontini.andromeda.modules.mechanics.throwable_items.data.events.types;

import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.Event;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.EventType;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.util.hit.HitResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BlockEvent extends Event {
    public BlockEvent(List<Command> commands, Optional<LootCondition> condition) {
        super(commands, condition);
    }

    @Override
    public boolean canRun(HitResult result) {
        return result.getType() == HitResult.Type.BLOCK;
    }

    @Override
    public Set<CommandType> allowed() {
        return Set.of(CommandType.ITEM, CommandType.USER, CommandType.SERVER, CommandType.HIT_BLOCK);
    }

    @Override
    public EventType type() {
        return EventType.BLOCK;
    }
}

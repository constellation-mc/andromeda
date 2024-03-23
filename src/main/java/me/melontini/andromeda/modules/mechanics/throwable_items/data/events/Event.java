package me.melontini.andromeda.modules.mechanics.throwable_items.data.events;

import lombok.CustomLog;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.util.hit.HitResult;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@CustomLog
@Getter
@Accessors(fluent = true)
public abstract class Event {

    private final List<Command> commands;
    private final Optional<LootCondition> condition;

    public Event(List<Command> commands, Optional<LootCondition> condition) {
        this.commands = commands;
        this.condition = condition;

        var allowed = new HashSet<>(allowed());
        allowed.addAll(CommandType.CONSTANT);
        this.commands.stream().filter(command -> !allowed.contains(command.type())).findFirst().ifPresent(command -> {
            throw new IllegalStateException("Unsupported command type: %s".formatted(CommandType.getId(command.type())));
        });
    }

    public void onCollision(Context context) {
        if (this.condition.map(condition1 -> condition1.test(context.lootContext())).orElse(true)) {
            commands.forEach(command -> command.execute(context));
        }
    }

    public abstract boolean canRun(HitResult result);

    public abstract Set<CommandType> allowed();

    public abstract EventType type();
}

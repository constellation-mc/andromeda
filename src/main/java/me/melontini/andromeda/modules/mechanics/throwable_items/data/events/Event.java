package me.melontini.andromeda.modules.mechanics.throwable_items.data.events;

import lombok.CustomLog;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@CustomLog
@Getter @Accessors(fluent = true)
public abstract class Event {

    private final List<Command> commands;
    private final Optional<LootCondition> condition;

    public Event(List<Command> commands, Optional<LootCondition> condition) {
        this.commands = commands;
        this.condition = condition;

        var allowed = allowed();
        this.commands.stream().filter(command -> !allowed.contains(command.type())).findFirst().ifPresent(command -> {
            throw new IllegalStateException("Unsupported command type: %s".formatted(command.type()));
        });
    }

    public void onCollision(ItemStack stack, FlyingItemEntity fie, ServerWorld world, @Nullable Entity user, HitResult hitResult, LootContext context) {
        if (this.condition.map(condition1 -> condition1.test(context)).orElse(true)) {
            commands.forEach(command -> command.execute(stack, fie, world, user, hitResult));
        }
    }

    public abstract boolean canRun(HitResult result);
    public abstract Set<CommandType> allowed();
    public abstract EventType type();
}

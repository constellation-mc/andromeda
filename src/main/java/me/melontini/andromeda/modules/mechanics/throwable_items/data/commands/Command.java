package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import net.minecraft.loot.condition.LootCondition;

import java.util.Optional;

@Getter
@AllArgsConstructor
public abstract class Command {

    protected final Optional<LootCondition> condition;

    public boolean tryExecute(Context context) {
        if (!this.condition.map(condition1 -> condition1.test(context.lootContext())).orElse(true)) return false;
        return execute(context);
    }

    protected abstract boolean execute(Context context);
    public abstract CommandType type();
}

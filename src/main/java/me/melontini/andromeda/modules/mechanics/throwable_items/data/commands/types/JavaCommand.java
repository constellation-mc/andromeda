package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import me.melontini.andromeda.modules.mechanics.throwable_items.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;

import java.util.Optional;

public class JavaCommand extends Command {

    private final ItemBehavior behavior;

    public JavaCommand(ItemBehavior behavior) {
        super(Optional.empty());
        this.behavior = behavior;
    }

    @Override
    protected boolean execute(Context context) {
        this.behavior.run(context);
        return true;
    }

    @Override
    public CommandType type() {
        return CommandType.JAVA;
    }

    @FunctionalInterface
    public interface ItemBehavior {
        void run(Context context);

    }
}

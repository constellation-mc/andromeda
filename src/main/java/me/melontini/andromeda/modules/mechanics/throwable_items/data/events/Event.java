package me.melontini.andromeda.modules.mechanics.throwable_items.data.events;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.JavaCommand;
import me.melontini.dark_matter.api.minecraft.data.ExtraCodecs;
import net.minecraft.loot.condition.LootCondition;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record Event(EventType type, List<Command> commands, Optional<LootCondition> condition) {

    public static final Codec<Event> CODEC = RecordCodecBuilder.create(data -> data.group(
            EventType.CODEC.fieldOf("type").forGetter(Event::type),
            ExtraCodecs.list(CommandType.DISPATCH).fieldOf("commands").forGetter(Event::commands),
            MiscUtil.LOOT_CONDITION_CODEC.optionalFieldOf("condition").forGetter(Event::condition)
    ).apply(data, Event::new));

    public void run(Context context) {
        if (this.condition.map(condition1 -> condition1.test(context.lootContext())).orElse(true)) {
            commands.forEach(command -> command.tryExecute(context));
        }
    }

    public static Event of(EventType type, JavaCommand.ItemBehavior behavior) {
        return new Event(type, Collections.singletonList(new JavaCommand(behavior)), Optional.empty());
    }
}

package me.melontini.andromeda.modules.mechanics.throwable_items.data.events;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.types.AnyEvent;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.types.BlockEvent;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.types.EntityEvent;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.types.MissEvent;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public record EventType(Codec<Event> codec) {

    public static final EventType BLOCK = new EventType(create(BlockEvent::new));
    public static final EventType ENTITY = new EventType(create(EntityEvent::new));
    public static final EventType MISS = new EventType(create(MissEvent::new));
    public static final EventType ANY = new EventType(create(AnyEvent::new));

    private static Codec<Event> create(BiFunction<List<Command>, Optional<LootCondition>, Event> function) {
        return RecordCodecBuilder.create(data -> data.group(
                MiscUtil.listCodec(CommandType.CODEC.dispatch("type", Command::type, CommandType::codec)).optionalFieldOf("commands", Collections.emptyList()).forGetter(Event::commands),
                MiscUtil.LOOT_CONDITION_CODEC.optionalFieldOf("condition").forGetter(Event::condition)
        ).apply(data, function));
    }

    private static final BiMap<Identifier, EventType> TYPE_MAP = ImmutableBiMap.<Identifier, EventType>builder()
            .put(Common.id("block"), BLOCK)
            .put(Common.id("entity"), ENTITY)
            .put(Common.id("miss"), MISS)
            .put(Common.id("any"), ANY)
            .build();

    public static final Codec<EventType> CODEC = Identifier.CODEC.flatXmap(identifier -> {
        EventType type = TYPE_MAP.get(identifier);
        if (type == null) return DataResult.error("Unknown event type: %s".formatted(identifier));
        return DataResult.success(type);
    }, eventType -> {
        Identifier identifier = TYPE_MAP.inverse().get(eventType);
        if (identifier == null) return DataResult.error("Unknown event type: %s".formatted(eventType));
        return DataResult.success(identifier);
    });
}

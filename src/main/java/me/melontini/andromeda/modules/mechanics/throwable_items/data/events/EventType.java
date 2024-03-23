package me.melontini.andromeda.modules.mechanics.throwable_items.data.events;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import me.melontini.andromeda.common.registries.Common;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public record EventType(Predicate<HitResult> predicate) {

    private static final BiMap<Identifier, EventType> TYPE_MAP = HashBiMap.create();

    public static final Codec<EventType> CODEC = Identifier.CODEC.flatXmap(identifier -> {
        EventType type = TYPE_MAP.get(identifier);
        if (type == null) return DataResult.error(() -> "Unknown event type: %s".formatted(identifier));
        return DataResult.success(type);
    }, eventType -> {
        Identifier identifier = TYPE_MAP.inverse().get(eventType);
        if (identifier == null) return DataResult.error(() -> "Unknown event type: %s".formatted(eventType));
        return DataResult.success(identifier);
    });

    public static EventType register(Identifier id, Predicate<HitResult> predicate) {
        EventType type = new EventType(predicate);
        TYPE_MAP.put(id, type);
        return type;
    }

    public static final EventType BLOCK = register(Common.id("block"), result -> result.getType() == HitResult.Type.BLOCK);
    public static final EventType ENTITY = register(Common.id("entity"), result -> result.getType() == HitResult.Type.ENTITY);
    public static final EventType MISS = register(Common.id("miss"), result -> result.getType() == HitResult.Type.MISS);
    public static final EventType ANY = register(Common.id("any"), result -> true);
}

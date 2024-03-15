package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.*;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public record CommandType(Codec<Command> codec) {

    public static final CommandType ITEM = new CommandType(create(ItemCommand::new));
    public static final CommandType USER = new CommandType(create(UserCommand::new));
    public static final CommandType SERVER = new CommandType(create(ServerCommand::new));
    public static final CommandType HIT_ENTITY = new CommandType(create(HitEntityCommand::new));
    public static final CommandType HIT_BLOCK = new CommandType(create(HitBlockCommand::new));

    private static Codec<Command> create(BiFunction<List<String>, ItemBehaviorData.Particles, Command> function) {
        return RecordCodecBuilder.create(data -> data.group(
                MiscUtil.listCodec(Codec.STRING).optionalFieldOf("commands", Collections.emptyList()).forGetter(Command::getCommands),
                ItemBehaviorData.Particles.CODEC.optionalFieldOf("particles", ItemBehaviorData.Particles.EMPTY).forGetter(Command::getParticles)
        ).apply(data, function));
    }

    private static final BiMap<Identifier, CommandType> TYPE_MAP = ImmutableBiMap.<Identifier, CommandType>builder()
            .put(Common.id("item"), ITEM)
            .put(Common.id("user"), USER)
            .put(Common.id("server"), SERVER)
            .put(Common.id("hit_entity"), HIT_ENTITY)
            .put(Common.id("hit_block"), HIT_BLOCK)
            .build();

    public static final Codec<CommandType> CODEC = Identifier.CODEC.flatXmap(identifier -> {
        CommandType type = TYPE_MAP.get(identifier);
        if (type == null) return DataResult.error("Unknown command type: %s".formatted(identifier));
        return DataResult.success(type);
    }, eventType -> {
        Identifier identifier = TYPE_MAP.inverse().get(eventType);
        if (identifier == null) return DataResult.error("Unknown command type: %s".formatted(eventType));
        return DataResult.success(identifier);
    });
}

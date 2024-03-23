package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.*;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.logic.AllOfCommand;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.logic.AnyOfCommand;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.logic.DefaultedCommand;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.logic.RandomCommand;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record CommandType(Codec<Command> codec) {

    private static final BiMap<Identifier, CommandType> TYPE_MAP = HashBiMap.create();
    public static final Set<CommandType> CONSTANT = new HashSet<>();

    public static final Codec<CommandType> CODEC = Identifier.CODEC.flatXmap(identifier -> {
        CommandType type = TYPE_MAP.get(identifier);
        if (type == null) return DataResult.error(() -> "Unknown command type: %s".formatted(identifier));
        return DataResult.success(type);
    }, eventType -> {
        Identifier identifier = TYPE_MAP.inverse().get(eventType);
        if (identifier == null) return DataResult.error(() -> "Unknown command type: %s".formatted(eventType));
        return DataResult.success(identifier);
    });
    public static final Codec<Command> DISPATCH = CommandType.CODEC.dispatch("type", Command::type, CommandType::codec);

    public static @Nullable Identifier getId(CommandType type) {
        return TYPE_MAP.inverse().get(type);
    }

    public static CommandType register(Identifier id, Codec<? extends Command> codec) {
        CommandType type = new CommandType((Codec<Command>) codec);
        TYPE_MAP.put(id, type);
        return type;
    }

    public static CommandType constant(CommandType type) {
        CONSTANT.add(type);
        return type;
    }

    public static final CommandType ITEM = constant(register(Common.id("item"), create(ItemCommand::new)));
    public static final CommandType USER = constant(register(Common.id("user"), create(UserCommand::new)));
    public static final CommandType SERVER = constant(register(Common.id("server"), create(ServerCommand::new)));
    public static final CommandType HIT_ENTITY = register(Common.id("hit_entity"), create(HitEntityCommand::new));
    public static final CommandType HIT_BLOCK = register(Common.id("hit_block"), create(HitBlockCommand::new));
    public static final CommandType RANDOM = constant(register(Common.id("random"), RandomCommand.CODEC));
    public static final CommandType DEFAULTED = constant(register(Common.id("defaulted"), DefaultedCommand.CODEC));
    public static final CommandType ALL_OF = constant(register(Common.id("all_of"), AllOfCommand.CODEC));
    public static final CommandType ANY_OF = constant(register(Common.id("any_of"), AnyOfCommand.CODEC));

    private static Codec<Command> create(Function3<List<String>, ItemBehaviorData.Particles, Optional<LootCondition>, Command> function) {
        return RecordCodecBuilder.create(data -> data.group(
                MiscUtil.listCodec(Codec.STRING).optionalFieldOf("commands", Collections.emptyList()).forGetter(Command::getCommands),
                ItemBehaviorData.Particles.CODEC.optionalFieldOf("particles", ItemBehaviorData.Particles.EMPTY).forGetter(Command::getParticles),
                MiscUtil.LOOT_CONDITION_CODEC.optionalFieldOf("condition").forGetter(Command::getCondition)
        ).apply(data, function));
    }
}

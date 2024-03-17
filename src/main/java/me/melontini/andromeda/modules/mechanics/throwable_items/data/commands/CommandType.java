package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.*;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.logic.AllOfCommand;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.logic.AnyOfCommand;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.logic.DefaultedCommand;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.logic.RandomCommand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

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

    public static final CommandType COMMANDS = constant(register(Common.id("commands"), CommandCommand.CODEC));
    public static final CommandType PARTICLES = constant(register(Common.id("particles"), ParticlesCommand.CODEC));
    public static final CommandType EXPLOSION = constant(register(Common.id("explosion"), ExplosionCommand.CODEC));
    public static final CommandType USE_ON_BLOCK = constant(register(Common.id("use_on_block"), UseOnBlockCommand.CODEC));


    public static final CommandType RANDOM = constant(register(Common.id("random"), RandomCommand.CODEC));
    public static final CommandType DEFAULTED = constant(register(Common.id("defaulted"), DefaultedCommand.CODEC));
    public static final CommandType ALL_OF = constant(register(Common.id("all_of"), AllOfCommand.CODEC));
    public static final CommandType ANY_OF = constant(register(Common.id("any_of"), AnyOfCommand.CODEC));

    public static final CommandType JAVA = constant(register(Common.id("java"), Codec.unit(new JavaCommand(context -> {}))));
}

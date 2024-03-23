package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Selector;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;

import java.util.Optional;

@Getter
@Accessors(fluent = true)
public class ExplosionCommand extends Command {

    private static final Codec<World.ExplosionSourceType> EST_CODEC = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(World.ExplosionSourceType.valueOf(string.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return DataResult.error(() -> "No such explosion type %s".formatted(string));
        }
    }, type -> type.name().toLowerCase());

    public static final Codec<ExplosionCommand> CODEC = RecordCodecBuilder.create(data -> data.group(
            Selector.CODEC.fieldOf("selector").forGetter(ExplosionCommand::selector),
            Codec.FLOAT.optionalFieldOf("power", 4f).forGetter(ExplosionCommand::power),
            EST_CODEC.optionalFieldOf("explosion_type", World.ExplosionSourceType.TNT).forGetter(ExplosionCommand::explosionSourceType),
            MiscUtil.LOOT_CONDITION_CODEC.optionalFieldOf("condition").forGetter(Command::getCondition)
    ).apply(data, ExplosionCommand::new));

    private final Selector selector;
    private final float power;
    private final World.ExplosionSourceType explosionSourceType;

    public ExplosionCommand(Selector selector, float power, World.ExplosionSourceType explosionSourceType, Optional<LootCondition> condition) {
        super(condition);
        this.selector = selector;
        this.power = power;
        this.explosionSourceType = explosionSourceType;
    }

    @Override
    protected boolean execute(Context context) {
        ServerCommandSource source = selector.function().apply(context);
        context.world().createExplosion(source.getEntity(),
                source.getPosition().getX(),
                source.getPosition().getY(),
                source.getPosition().getZ(),
                power, explosionSourceType);
        return true;
    }

    @Override
    public CommandType type() {
        return CommandType.EXPLOSION;
    }
}

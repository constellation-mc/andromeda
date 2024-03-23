package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import me.melontini.dark_matter.api.minecraft.data.ExtraCodecs;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.collection.WeightedList;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;

@Getter @Accessors(fluent = true)
public class RandomCommand extends Command {

    public static final Codec<RandomCommand> CODEC = RecordCodecBuilder.create(data -> data.group(
            ExtraCodecs.weightedList(CommandType.DISPATCH).fieldOf("commands").forGetter(RandomCommand::commands),
            Codec.INT.optionalFieldOf("rolls", 1).forGetter(RandomCommand::rolls),
            MiscUtil.LOOT_CONDITION_CODEC.optionalFieldOf("condition").forGetter(Command::getCondition)
    ).apply(data, RandomCommand::new));

    private final WeightedList<Command> commands;
    private final int rolls;

    public RandomCommand(WeightedList<Command> commands, int rolls, Optional<LootCondition> condition) {
        super(Collections.emptyList(), ItemBehaviorData.Particles.EMPTY, condition);
        this.commands = commands;
        this.rolls = rolls;
    }

    @Override
    public boolean execute(Context context) {
        if (!this.condition.map(condition1 -> condition1.test(context.lootContext())).orElse(true)) return false;

        boolean b = false;
        for (int i = 0; i < rolls; i++) {
            var itr = this.commands.shuffle().iterator();
            if (itr.hasNext()) b |= itr.next().execute(context);
        }
        return b;
    }

    @Override
    protected @Nullable ServerCommandSource createSource(Context context) {
        return null;
    }

    @Override
    public CommandType type() {
        return CommandType.RANDOM;
    }
}

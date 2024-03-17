package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Optional;

public class UseOnBlockCommand extends Command {

    public static final Codec<UseOnBlockCommand> CODEC = RecordCodecBuilder.create(data -> data.group(
            MiscUtil.LOOT_CONDITION_CODEC.optionalFieldOf("condition").forGetter(Command::getCondition)
    ).apply(data, UseOnBlockCommand::new));

    public UseOnBlockCommand(Optional<LootCondition> condition) {
        super(condition);
    }

    @Override
    protected boolean execute(Context context) {
        if (context.hitResult().getType() != HitResult.Type.BLOCK) return false;

        context.stack().getItem().useOnBlock(new ItemUsageContext(
                context.world(), context.user() instanceof PlayerEntity player ? player : null,
                Hand.MAIN_HAND, context.stack(), (BlockHitResult) context.hitResult()
        ));
        return true;
    }

    @Override
    public CommandType type() {
        return CommandType.USE_ON_BLOCK;
    }
}

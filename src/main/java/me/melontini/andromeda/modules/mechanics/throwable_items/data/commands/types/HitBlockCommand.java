package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class HitBlockCommand extends Command {
    public HitBlockCommand(List<String> commands, ItemBehaviorData.Particles particles, Optional<LootCondition> condition) {
        super(commands, particles, condition);
    }

    @Override
    protected @Nullable ServerCommandSource createSource(Context context) {
        var world = context.world(); var hitResult = context.hitResult();

        if (hitResult.getType() != HitResult.Type.BLOCK) return null;
        BlockHitResult hit = (BlockHitResult) hitResult;

        return new ServerCommandSource(world.getServer(),
                new Vec3d(hit.getBlockPos().getX(),
                        hit.getBlockPos().getY(),
                        hit.getBlockPos().getZ()), Vec2f.ZERO,
                world, 4, "Block", TextUtil.literal("Block"),
                world.getServer(), context.fie());
    }

    @Override
    public CommandType type() {
        return CommandType.HIT_BLOCK;
    }
}

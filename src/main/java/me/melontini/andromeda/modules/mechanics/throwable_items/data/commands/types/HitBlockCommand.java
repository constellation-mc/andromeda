package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HitBlockCommand extends Command {
    public HitBlockCommand(List<String> commands, ItemBehaviorData.Particles particles) {
        super(commands, particles);
    }

    @Override
    protected @Nullable ServerCommandSource createSource(ItemStack stack, FlyingItemEntity fie, ServerWorld world, @Nullable Entity user, HitResult hitResult) {
        if (hitResult.getType() != HitResult.Type.BLOCK) return null;
        BlockHitResult hit = (BlockHitResult) hitResult;

        return new ServerCommandSource(world.getServer(),
                new Vec3d(hit.getBlockPos().getX(),
                        hit.getBlockPos().getY(),
                        hit.getBlockPos().getZ()), Vec2f.ZERO,
                world, 4, "Block", TextUtil.literal("Block"),
                world.getServer(), fie).withSilent();
    }

    @Override
    public CommandType type() {
        return CommandType.HIT_BLOCK;
    }
}

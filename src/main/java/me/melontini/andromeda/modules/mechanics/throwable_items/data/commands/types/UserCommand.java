package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UserCommand extends Command {
    public UserCommand(List<String> commands, ItemBehaviorData.Particles particles) {
        super(commands, particles);
    }

    @Override
    @Nullable
    protected ServerCommandSource createSource(ItemStack stack, FlyingItemEntity fie, ServerWorld world, @Nullable Entity user, HitResult hitResult) {
        if (user == null) return null;

        return new ServerCommandSource(world.getServer(), user.getPos(),
                new Vec2f(user.getPitch(), user.getYaw()),
                world, 4, user.getEntityName(), user.getName(),
                world.getServer(), user).withSilent();
    }

    @Override
    public CommandType type() {
        return CommandType.USER;
    }
}

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

public class ItemCommand extends Command {
    public ItemCommand(List<String> commands, ItemBehaviorData.Particles particles) {
        super(commands, particles);
    }

    @Override
    protected ServerCommandSource createSource(ItemStack stack, FlyingItemEntity fie, ServerWorld world, @Nullable Entity user, HitResult hitResult) {
        return new ServerCommandSource(world.getServer(), fie.getPos(),
                new Vec2f(fie.getPitch(), fie.getYaw()),
                world, 4, fie.getNameForScoreboard(), fie.getName(),
                world.getServer(), fie).withSilent();
    }

    @Override
    public CommandType type() {
        return CommandType.ITEM;
    }
}

package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import me.melontini.andromeda.modules.mechanics.throwable_items.FlyingItemEntity;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HitEntityCommand extends Command {
    public HitEntityCommand(List<String> commands, ItemBehaviorData.Particles particles) {
        super(commands, particles);
    }

    @Override
    protected @Nullable ServerCommandSource createSource(ItemStack stack, FlyingItemEntity fie, ServerWorld world, @Nullable Entity user, HitResult hitResult) {
        if (hitResult.getType() != HitResult.Type.ENTITY) return null;

        EntityHitResult entityHitResult = (EntityHitResult) hitResult;
        Entity entity = entityHitResult.getEntity();

        return new ServerCommandSource(world.getServer(), entity.getPos(),
                new Vec2f(entity.getPitch(), entity.getYaw()),
                world, 4, entity.getEntityName(), entity.getName(),
                world.getServer(), entity).withSilent();
    }

    @Override
    public CommandType type() {
        return CommandType.HIT_ENTITY;
    }
}

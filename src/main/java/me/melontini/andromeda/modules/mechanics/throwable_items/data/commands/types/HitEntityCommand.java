package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types;

import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class HitEntityCommand extends Command {
    public HitEntityCommand(List<String> commands, ItemBehaviorData.Particles particles, Optional<LootCondition> condition) {
        super(commands, particles, condition);
    }

    @Override
    protected @Nullable ServerCommandSource createSource(Context context) {
        var world = context.world(); var hitResult = context.hitResult();

        if (hitResult.getType() != HitResult.Type.ENTITY) return null;

        EntityHitResult entityHitResult = (EntityHitResult) hitResult;
        Entity entity = entityHitResult.getEntity();

        return new ServerCommandSource(world.getServer(), entity.getPos(),
                new Vec2f(entity.getPitch(), entity.getYaw()),
                world, 4, entity.getEntityName(), entity.getName(),
                world.getServer(), entity);
    }

    @Override
    public CommandType type() {
        return CommandType.HIT_ENTITY;
    }
}

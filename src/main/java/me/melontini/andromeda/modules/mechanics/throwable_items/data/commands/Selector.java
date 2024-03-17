package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@SuppressWarnings("unused")
public record Selector(Function<Context, ServerCommandSource> function) {

    private static final BiMap<Identifier, Selector> TYPE_MAP = HashBiMap.create();

    public static final Codec<Selector> CODEC = Identifier.CODEC.flatXmap(identifier -> {
        Selector type = TYPE_MAP.get(identifier);
        if (type == null) return DataResult.error(() -> "Unknown command type: %s".formatted(identifier));
        return DataResult.success(type);
    }, eventType -> {
        Identifier identifier = TYPE_MAP.inverse().get(eventType);
        if (identifier == null) return DataResult.error(() -> "Unknown command type: %s".formatted(eventType));
        return DataResult.success(identifier);
    });

    public static @Nullable Identifier getId(Selector type) {
        return TYPE_MAP.inverse().get(type);
    }

    public static Selector register(Identifier id, Function<Context, ServerCommandSource> function) {
        Selector selector = new Selector(function);
        TYPE_MAP.put(id, selector);
        return selector;
    }

    public static final Selector USER = register(Common.id("user"), context -> {
        var world = context.world(); var user = context.user();
        if (user == null) return null;

        return new ServerCommandSource(world.getServer(), user.getPos(),
                new Vec2f(user.getPitch(), user.getYaw()),
                world, 4, user.getEntityName(), user.getName(),
                world.getServer(), user);
    });

    public static final Selector ITEM = register(Common.id("item"), context -> {
        var world = context.world(); var fie = context.fie();
        return new ServerCommandSource(world.getServer(), fie.getPos(),
                new Vec2f(fie.getPitch(), fie.getYaw()),
                world, 4, fie.getEntityName(), fie.getName(),
                world.getServer(), fie);
    });

    public static final Selector HIT_BLOCK = register(Common.id("hit_block"), context -> {
        var world = context.world(); var hitResult = context.hitResult();

        if (hitResult.getType() != HitResult.Type.BLOCK) return null;
        BlockHitResult hit = (BlockHitResult) hitResult;

        return new ServerCommandSource(world.getServer(),
                new Vec3d(hit.getBlockPos().getX(),
                        hit.getBlockPos().getY(),
                        hit.getBlockPos().getZ()), Vec2f.ZERO,
                world, 4, "Block", TextUtil.literal("Block"),
                world.getServer(), context.fie());
    });

    public static final Selector HIT_ENTITY = register(Common.id("hit_entity"), context -> {
        var world = context.world(); var hitResult = context.hitResult();

        if (hitResult.getType() != HitResult.Type.ENTITY) return null;

        EntityHitResult entityHitResult = (EntityHitResult) hitResult;
        Entity entity = entityHitResult.getEntity();

        return new ServerCommandSource(world.getServer(), entity.getPos(),
                new Vec2f(entity.getPitch(), entity.getYaw()),
                world, 4, entity.getEntityName(), entity.getName(),
                world.getServer(), entity);
    });

    public static final Selector SERVER = register(Common.id("server"), context -> context.world().getServer().getCommandSource());
}

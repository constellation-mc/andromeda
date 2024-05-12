package me.melontini.andromeda.common.util;

import com.google.common.base.Supplier;
import lombok.experimental.UtilityClass;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.minecraft.loot.context.LootContextParameters.*;
import static net.minecraft.loot.context.LootContextTypes.*;

@UtilityClass
public class LootContextUtil {

    public static Supplier<LootContext> empty(World world) {
        return () -> build(setBuilder((ServerWorld) world).build(EMPTY));
    }

    public static Supplier<LootContext> command(World world, Vec3d origin, @Nullable Entity entity) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin).addOptional(THIS_ENTITY, entity).build(COMMAND));
    }

    public static Supplier<LootContext> command(World world, Vec3d origin) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin).build(COMMAND));
    }

    public static Supplier<LootContext> fishing(World world, Vec3d origin, @Nullable ItemStack tool, @Nullable Entity entity) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin)
                .add(TOOL, Objects.requireNonNullElse(tool, ItemStack.EMPTY))
                .addOptional(THIS_ENTITY, entity).build(FISHING));
    }

    public static Supplier<LootContext> fishing(World world, Vec3d origin, @Nullable ItemStack tool) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin)
                .add(TOOL, Objects.requireNonNullElse(tool, ItemStack.EMPTY))
                .build(FISHING));
    }

    public static Supplier<LootContext> fishing(World world, Vec3d origin) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin)
                .add(TOOL, ItemStack.EMPTY).build(FISHING));
    }

    public static Supplier<LootContext> entity(World world, Vec3d origin, Entity entity, @Nullable DamageSource source, @Nullable Entity killer, @Nullable Entity directKiller, @Nullable PlayerEntity lastDamagePlayer) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin)
                .add(THIS_ENTITY, entity).add(DAMAGE_SOURCE, Objects.requireNonNullElseGet(source, () -> world.getDamageSources().generic()))
                .addOptional(KILLER_ENTITY, killer).addOptional(DIRECT_KILLER_ENTITY, directKiller)
                .addOptional(LAST_DAMAGE_PLAYER, lastDamagePlayer)
                .build(ENTITY));
    }

    public static Supplier<LootContext> entity(World world, Vec3d origin, Entity entity, @Nullable DamageSource source, @Nullable Entity killer, @Nullable Entity directKiller) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin)
                .add(THIS_ENTITY, entity).add(DAMAGE_SOURCE, Objects.requireNonNullElseGet(source, () -> world.getDamageSources().generic()))
                .addOptional(KILLER_ENTITY, killer).addOptional(DIRECT_KILLER_ENTITY, directKiller)
                .build(ENTITY));
    }

    public static Supplier<LootContext> entity(World world, Vec3d origin, Entity entity, @Nullable DamageSource source, @Nullable Entity killer) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin)
                .add(THIS_ENTITY, entity).add(DAMAGE_SOURCE, Objects.requireNonNullElseGet(source, () -> world.getDamageSources().generic()))
                .addOptional(KILLER_ENTITY, killer)
                .build(ENTITY));
    }

    public static Supplier<LootContext> entity(World world, Vec3d origin, Entity entity, @Nullable DamageSource source) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin)
                .add(THIS_ENTITY, entity).add(DAMAGE_SOURCE, Objects.requireNonNullElseGet(source, () -> world.getDamageSources().generic()))
                .build(ENTITY));
    }

    public static Supplier<LootContext> entity(World world, Vec3d origin, Entity entity) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin)
                .add(THIS_ENTITY, entity).add(DAMAGE_SOURCE, world.getDamageSources().generic())
                .build(ENTITY));
    }

    public static Supplier<LootContext> block(World world, Vec3d origin, BlockState state, @Nullable ItemStack stack, @Nullable Entity entity, @Nullable BlockEntity blockEntity, float radius) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin).add(BLOCK_STATE, state)
                .add(TOOL, Objects.requireNonNullElse(stack, ItemStack.EMPTY))
                .addOptional(THIS_ENTITY, entity).addOptional(BLOCK_ENTITY, blockEntity)
                .addOptional(EXPLOSION_RADIUS, radius).build(BLOCK));
    }

    public static Supplier<LootContext> block(World world, Vec3d origin, BlockState state, @Nullable ItemStack stack, @Nullable Entity entity, @Nullable BlockEntity blockEntity) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin).add(BLOCK_STATE, state)
                .add(TOOL, Objects.requireNonNullElse(stack, ItemStack.EMPTY))
                .addOptional(THIS_ENTITY, entity).addOptional(BLOCK_ENTITY, blockEntity)
                .build(BLOCK));
    }

    public static Supplier<LootContext> block(World world, Vec3d origin, BlockState state, @Nullable ItemStack stack, @Nullable Entity entity) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin).add(BLOCK_STATE, state)
                .add(TOOL, Objects.requireNonNullElse(stack, ItemStack.EMPTY))
                .addOptional(THIS_ENTITY, entity)
                .build(BLOCK));
    }

    public static Supplier<LootContext> block(World world, Vec3d origin, BlockState state, @Nullable ItemStack stack) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin).add(BLOCK_STATE, state)
                .add(TOOL, Objects.requireNonNullElse(stack, ItemStack.EMPTY))
                .build(BLOCK));
    }

    public static Supplier<LootContext> block(World world, Vec3d origin, BlockState state) {
        return () -> build(setBuilder((ServerWorld) world).add(ORIGIN, origin).add(BLOCK_STATE, state)
                .add(TOOL, ItemStack.EMPTY)
                .build(BLOCK));
    }

    public static LootContextParameterSet.Builder setBuilder(ServerWorld world) {
        return new LootContextParameterSet.Builder(world);
    }

    public static LootContext build(LootContextParameterSet set) {
        return new LootContext.Builder(set).build(null);
    }
}

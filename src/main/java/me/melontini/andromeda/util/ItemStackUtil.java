package me.melontini.andromeda.util;

import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.MathStuff;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ItemStackUtil {

    public static ItemStack getStackOrEmpty(ItemConvertible item) {
        return Optional.ofNullable(item).map(ItemConvertible::asItem).map(Item::getDefaultStack).orElse(ItemStack.EMPTY);
    }

    public static void spawn(@NotNull BlockPos pos, ItemStack stack, World world) {
        MakeSure.notNulls(pos, stack, world);
        ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }

    public static void spawn(@NotNull Vec3d pos, ItemStack stack, World world) {
        MakeSure.notNulls(pos, stack, world);
        ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }

    public static void spawnVelocity(@NotNull BlockPos pos, ItemStack stack, World world, double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        MakeSure.notNulls(pos, stack, world);
        ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack,
                MathStuff.nextDouble(minX, maxX), MathStuff.nextDouble(minY, maxY), MathStuff.nextDouble(minZ, maxZ));
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }

    public static void spawnVelocity(@NotNull Vec3d pos, ItemStack stack, World world, double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        MakeSure.notNulls(pos, stack, world);
        ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack,
                MathStuff.nextDouble(minX, maxX), MathStuff.nextDouble(minY, maxY), MathStuff.nextDouble(minZ, maxZ));
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }

    public static void spawnVelocity(@NotNull BlockPos pos, ItemStack stack, World world, Vec3d vec3d) {
        MakeSure.notNulls(pos, stack, world);
        ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack,
                vec3d.x, vec3d.y, vec3d.z);
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }

    public static void spawnVelocity(@NotNull Vec3d pos, ItemStack stack, World world, Vec3d vec3d) {
        MakeSure.notNulls(pos, stack, world);
        ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack,
                vec3d.x, vec3d.y, vec3d.z);
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }
}

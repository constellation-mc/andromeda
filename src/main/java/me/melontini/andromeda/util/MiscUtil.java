package me.melontini.andromeda.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MiscUtil {

    public static Optional<RegistryKey<World>> getLodestoneDimension(NbtCompound nbt) {
        return World.CODEC.parse(NbtOps.INSTANCE, nbt.get("LodestoneDimension")).result();
    }

    @Nullable
    public static GlobalPos createLodestonePos(NbtCompound nbt) {
        boolean bl = nbt.contains("LodestonePos");
        boolean bl2 = nbt.contains("LodestoneDimension");
        if (bl && bl2) {
            Optional<RegistryKey<World>> optional = getLodestoneDimension(nbt);
            if (optional.isPresent()) {
                BlockPos blockPos = NbtHelper.toBlockPos(nbt.getCompound("LodestonePos"));
                return GlobalPos.create(optional.get(), blockPos);
            }
        }

        return null;
    }

    @Nullable
    public static GlobalPos createSpawnPos(World world) {
        return world.getDimension().isNatural() ? GlobalPos.create(world.getRegistryKey(), Utilities.supply(() -> {
            BlockPos blockPos = new BlockPos(world.getLevelProperties().getSpawnX(), world.getLevelProperties().getSpawnY(), world.getLevelProperties().getSpawnZ());
            if (!world.getWorldBorder().contains(blockPos)) {
                blockPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, new BlockPos(world.getWorldBorder().getCenterX(), 0.0, world.getWorldBorder().getCenterZ()));
            }
            return blockPos;
        })) : null;
    }

    public static double horizontalDistanceTo(Vec3d owner, Vec3d target) {
        double d = target.x - owner.x;
        double f = target.z - owner.z;
        return Math.sqrt(d * d + f * f);
    }

    public static String blockPosAsString(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    public static String vec3dAsString(Vec3d vec3d) {
        return vec3d.getX() + ", " + vec3d.getY() + ", " + vec3d.getZ();
    }

}

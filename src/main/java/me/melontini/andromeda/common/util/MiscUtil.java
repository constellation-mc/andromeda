package me.melontini.andromeda.common.util;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Function;

public class MiscUtil {

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

    public static BlockPos vec3dAsBlockPos(Vec3d vec3d) {
        return new BlockPos(MathHelper.floor(vec3d.x), MathHelper.floor(vec3d.y), MathHelper.floor(vec3d.z));
    }

    public static RegistryEntry.Reference<DamageType> getTypeReference(World world, RegistryKey<DamageType> type) {
        return world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).getEntry(type).orElseThrow();
    }

    public static <T> Codec<List<T>> listCodec(Codec<T> codec) {
        return Codec.either(codec, codec.listOf()).xmap(e -> e.map(ImmutableList::of, Function.identity()), Either::right);
    }

    public static <T> Codec<WeightedList<T>> weightedListCodec(Codec<T> codec) {
        return Codec.either(codec, WeightedList.createCodec(codec)).xmap(e -> e.map(entry -> {
            WeightedList<T> list = new WeightedList<>();
            list.add(entry, 1);
            return list;
        }, Function.identity()), Either::right);
    }
}

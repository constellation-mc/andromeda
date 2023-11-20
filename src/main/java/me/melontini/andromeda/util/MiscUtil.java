package me.melontini.andromeda.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class MiscUtil {

    public static boolean testModVersion(String modId, String predicate) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
        if (mod.isPresent()) {
            try {
                VersionPredicate version = VersionPredicate.parse(predicate);
                return version.test(mod.get().getMetadata().getVersion());
            } catch (VersionParsingException e) {
                return false;
            }
        }
        return false;
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

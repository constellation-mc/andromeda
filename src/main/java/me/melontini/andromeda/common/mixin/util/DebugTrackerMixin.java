package me.melontini.andromeda.common.mixin.util;

import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.common.util.DebugTracker;
import me.melontini.dark_matter.api.minecraft.debug.ValueTracker;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.function.Supplier;

@SpecialEnvironment(Environment.CLIENT)
@Mixin(value = DebugTracker.class, remap = false)
abstract class DebugTrackerMixin {

    /**
     * @author melontini
     * @reason client-only
     */
    @Overwrite
    public static void addTracker(@NotNull String s, @NotNull Supplier<?> supplier) {
        ValueTracker.addTracker(s, supplier);
    }

    /**
     * @author melontini
     * @reason client-only
     */
    @Overwrite
    public static void addTracker(@NotNull String s, @NotNull Supplier<?> supplier, @NotNull Duration duration) {
        ValueTracker.addTracker(s, supplier, duration);
    }

    /**
     * @author melontini
     * @reason client-only
     */
    @Overwrite
    public static void addTracker(@NotNull String s, @NotNull Field f, @NotNull Object o) {
        ValueTracker.addTracker(s, f, o);
    }

    /**
     * @author melontini
     * @reason client-only
     */
    @Overwrite
    public static void addTracker(@NotNull String s, @NotNull Field f, @NotNull Object o, @NotNull Duration duration) {
        ValueTracker.addTracker(s, f, o, duration);
    }

    /**
     * @author melontini
     * @reason client-only
     */
    @Overwrite
    public static void addTracker(@NotNull String s, @NotNull Field f) {
        ValueTracker.addTracker(s, f);
    }

    /**
     * @author melontini
     * @reason client-only
     */
    @Overwrite
    public static void addTracker(@NotNull String s, @NotNull Field f, @NotNull Duration duration) {
        ValueTracker.addTracker(s, f, duration);
    }

    /**
     * @author melontini
     * @reason client-only
     */
    @Overwrite
    public static void removeTracker(@NotNull String s) {
        ValueTracker.removeTracker(s);
    }
}

package me.melontini.andromeda.common;

import me.melontini.andromeda.base.Debug;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.minecraft.debug.ValueTracker;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class DebugTracker {

    public static void addTracker(@NotNull String s, @NotNull Supplier<?> supplier) {
        if (Debug.hasKey(Debug.Keys.DISPLAY_TRACKED_VALUES))
            Support.run(EnvType.CLIENT, () -> () -> ValueTracker.addTracker(s, supplier));
    }

    public static void addTracker(@NotNull String s, @NotNull Supplier<?> supplier, @NotNull Duration duration) {
        if (Debug.hasKey(Debug.Keys.DISPLAY_TRACKED_VALUES))
            Support.run(EnvType.CLIENT, () -> () -> ValueTracker.addTracker(s, supplier, duration));
    }

    public static void addTracker(@NotNull String s, @NotNull Field f, @NotNull Object o) {
        if (Debug.hasKey(Debug.Keys.DISPLAY_TRACKED_VALUES))
            Support.run(EnvType.CLIENT, () -> () -> ValueTracker.addTracker(s, f, o));
    }

    public static void addTracker(@NotNull String s, @NotNull Field f, @NotNull Object o, @NotNull Duration duration) {
        if (Debug.hasKey(Debug.Keys.DISPLAY_TRACKED_VALUES))
            Support.run(EnvType.CLIENT, () -> () -> ValueTracker.addTracker(s, f, o, duration));
    }

    public static void addTracker(@NotNull String s, @NotNull Field f) {
        if (Debug.hasKey(Debug.Keys.DISPLAY_TRACKED_VALUES))
            Support.run(EnvType.CLIENT, () -> () -> ValueTracker.addTracker(s, f));
    }

    public static void addTracker(@NotNull String s, @NotNull Field f, @NotNull Duration duration) {
        if (Debug.hasKey(Debug.Keys.DISPLAY_TRACKED_VALUES))
            Support.run(EnvType.CLIENT, () -> () -> ValueTracker.addTracker(s, f, duration));
    }

    public static void removeTracker(@NotNull String s) {
        Support.run(EnvType.CLIENT, () -> () -> ValueTracker.removeTracker(s));
    }
}

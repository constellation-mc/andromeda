package me.melontini.andromeda.common.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;


public class DebugTracker {

    public static final Duration FIVE_S = Duration.of(5, ChronoUnit.SECONDS);
    public static final Duration TEN_S = Duration.of(10, ChronoUnit.SECONDS);

    public static void addTracker(@NotNull String s, @NotNull Supplier<?> supplier) {
    }

    public static void addTracker(@NotNull String s, @NotNull Supplier<?> supplier, @NotNull Duration duration) {
    }

    public static void addTracker(@NotNull String s, @NotNull Field f, @NotNull Object o) {
    }

    public static void addTracker(@NotNull String s, @NotNull Field f, @NotNull Object o, @NotNull Duration duration) {
    }

    public static void addTracker(@NotNull String s, @NotNull Field f) {
    }

    public static void addTracker(@NotNull String s, @NotNull Field f, @NotNull Duration duration) {
    }

    public static void removeTracker(@NotNull String s) {
    }
}

package me.melontini.andromeda.api.config;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.config.FeatureManager;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

public final class FeatureConfig {

    public static Field set(String feature, Object value) throws NoSuchFieldException {
        return Config.set(feature, value);
    }

    public static <T> T get(String feature) throws NoSuchFieldException {
        return Config.get(feature);
    }

    public static void registerReason(String id, BiFunction<String, String, TextEntry> reason) {
        FeatureManager.ENTRIES.putIfAbsent(id, reason);
    }
}

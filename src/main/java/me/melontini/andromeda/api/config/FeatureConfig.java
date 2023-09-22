package me.melontini.andromeda.api.config;

import me.melontini.andromeda.config.Config;

import java.lang.reflect.Field;

public final class FeatureConfig {

    public static Field set(String feature, Object value) throws NoSuchFieldException {
        return Config.set(feature, value);
    }

    public static <T> T get(String feature) throws NoSuchFieldException {
        return Config.get(feature);
    }

    public static <T> T get(@SuppressWarnings("unused") Class<T> type, String feature) throws NoSuchFieldException {
        return Config.get(feature);
    }
}

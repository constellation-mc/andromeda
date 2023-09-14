package me.melontini.andromeda.api.config;

import me.melontini.andromeda.config.ConfigHelper;

import java.lang.reflect.Field;

public final class FeatureConfig {

    public static Field set(String feature, Object value) throws NoSuchFieldException, IllegalAccessException {
        return ConfigHelper.set(feature, value);
    }

    public static <T> T get(String feature) throws NoSuchFieldException, IllegalAccessException {
        return ConfigHelper.get(feature);
    }
}

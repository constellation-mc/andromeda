package me.melontini.andromeda.api;

import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.config.ConfigHelper;
import me.melontini.andromeda.config.FeatureManager;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Meant to be used with the {@code andromeda:feature_manager} entrypoint, implementing the {@link java.lang.Runnable} interface.
 */
public final class FeatureConfig {

    public static void registerProcessor(String id, Processor processor) {
        FeatureManager.registerProcessor(id, processor);
    }

    public static Field setOption(String option, Object value) throws NoSuchFieldException, IllegalAccessException {
        return ConfigHelper.setConfigOption(option, value);
    }

    public static <T> T getOption(String option) throws NoSuchFieldException, IllegalAccessException {
        return ConfigHelper.getConfigOption(option);
    }

    public interface Processor {
        @Nullable Map<String, Object> process(AndromedaConfig config);
    }
}

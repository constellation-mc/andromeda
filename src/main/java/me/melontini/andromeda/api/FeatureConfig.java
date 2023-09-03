package me.melontini.andromeda.api;

import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.config.FeatureManager;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Meant to be used with the {@code andromeda:feature_manager} entrypoint, implementing the {@link java.lang.Runnable} interface.
 */
public final class FeatureConfig {

    public static void registerProcessor(String id, Processor processor) {
        FeatureManager.registerProcessor(id, processor);
    }

    public interface Processor {
        @Nullable Map<String, Object> process(AndromedaConfig config);
    }
}

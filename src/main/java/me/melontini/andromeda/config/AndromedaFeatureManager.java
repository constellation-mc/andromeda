package me.melontini.andromeda.config;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Deprecated
public class AndromedaFeatureManager {

    public static void registerProcessor(String id, FeatureProcessor processor) {
        FeatureManager.legacyRegister(id, processor::process);
        FeatureManager.LOGGER.info("Using AndromedaFeatureManager is deprecated. Processor: " + id);
    }

    public interface FeatureProcessor {
        @Nullable Map<String, Object> process(AndromedaConfig config);
    }
}

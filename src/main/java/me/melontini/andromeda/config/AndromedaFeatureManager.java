package me.melontini.andromeda.config;

import lombok.CustomLog;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Deprecated
@CustomLog
public class AndromedaFeatureManager {

    public static void registerProcessor(String id, FeatureProcessor processor) {
        FeatureManager.legacyRegister(id, processor::process);
        LOGGER.info("Using AndromedaFeatureManager is deprecated. Processor: " + id);
    }

    public interface FeatureProcessor {
        @Nullable Map<String, Object> process(AndromedaConfig config);
    }
}

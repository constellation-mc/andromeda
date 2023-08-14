package me.melontini.andromeda.config;

import me.melontini.andromeda.util.SharedConstants;

import java.util.HashMap;
import java.util.Map;

public class AndromedaFeatureManager {

    private static final Map<String, FeatureProcessor> features = new HashMap<>();

    private static void registerDefaultProcessors() {
        registerProcessor("connector_mod", config -> {
            if (SharedConstants.PLATFORM == SharedConstants.Platform.CONNECTOR) {
                config.compatMode = true;
                config.totemSettings.enableInfiniteTotem = false;
                config.totemSettings.enableTotemAscension = false;
                config.quickFire = false;
            }
        });
    }

    public static void registerProcessor(String feature, FeatureProcessor processor) {
        features.putIfAbsent(feature, processor);
    }

    public static void unregisterProcessor(String feature) {
        features.remove(feature);
    }

    public static void processFeatures(AndromedaConfig config) {
        features.forEach((feature, processor) -> processor.process(config));
    }

    public interface FeatureProcessor {
        void process(AndromedaConfig config);
    }

    static {
        registerDefaultProcessors();
    }

}

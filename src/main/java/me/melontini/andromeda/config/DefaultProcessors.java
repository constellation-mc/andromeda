package me.melontini.andromeda.config;

import me.melontini.andromeda.api.FeatureConfig;

import java.util.Map;

public class DefaultProcessors implements Runnable {
    @Override
    public void run() {

        FeatureConfig.registerProcessor("safe_beds_conflict", config -> {
            if (config.safeBeds) {
                return Map.of("bedsExplodeEverywhere", false);
            }
            if (config.bedsExplodeEverywhere) {
                return Map.of("safeBeds", false);
            }
            return null;
        });

        FeatureConfig.registerProcessor("iceberg", config -> {
            if (FeatureManager.testModVersion("minecraft", ">=1.20") &&
                    FeatureManager.testModVersion("iceberg", "<1.1.13")) {
                return Map.of(
                        "tooltipNotName", false,
                        "itemFrameTooltips", false
                );
            }
            return null;
        });
    }
}

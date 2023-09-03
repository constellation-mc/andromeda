package me.melontini.andromeda.config;

import me.melontini.andromeda.api.FeatureConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

import java.util.Map;
import java.util.Optional;

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
            Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer("iceberg");
            Optional<ModContainer> minecraft = FabricLoader.getInstance().getModContainer("minecraft");
            if (mod.isPresent() && minecraft.isPresent()) {
                try {
                    if (minecraft.get().getMetadata().getVersion().compareTo(Version.parse("1.20")) >= 0
                            && mod.get().getMetadata().getVersion().compareTo(Version.parse("1.1.13")) < 0) {
                        return Map.of(
                                "tooltipNotName", false,
                                "itemFrameTooltips", false
                        );
                    }
                } catch (VersionParsingException e) {
                    return null;
                }
            }
            return null;
        });
    }
}

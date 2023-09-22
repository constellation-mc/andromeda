package me.melontini.andromeda.config;

import me.melontini.andromeda.util.SharedConstants;
import me.melontini.dark_matter.api.config.OptionProcessorRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class DefaultProcessors {

    static void collect(OptionProcessorRegistry<AndromedaConfig> registry) {
        registry.register("andromeda:connector_mod", config -> {
            if (SharedConstants.PLATFORM == SharedConstants.Platform.CONNECTOR) {
                return Map.of(
                        "compatMode", true,
                        "totemSettings.enableInfiniteTotem", false,
                        "totemSettings.enableTotemAscension", false,
                        "quickFire", false
                );
            }
            return null;
        });

        registry.register("andromeda:safe_beds_conflict", manager -> {
            if (manager.getConfig().safeBeds) {
                return Map.of("bedsExplodeEverywhere", false);
            }
            if (manager.getConfig().bedsExplodeEverywhere) {
                return Map.of("safeBeds", false);
            }
            return null;
        });

        registry.register("andromeda:iceberg", config -> {
            if (testModVersion("minecraft", ">=1.20") &&
                    testModVersion("iceberg", "<1.1.13")) {
                return Map.of(
                        "tooltipNotName", false,
                        "itemFrameTooltips", false
                );
            }
            return null;
        });
    }

    static boolean testModVersion(String modId, String predicate) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
        if (mod.isPresent()) {
            try {
                VersionPredicate version = VersionPredicate.parse(predicate);
                return version.test(mod.get().getMetadata().getVersion());
            } catch (VersionParsingException e) {
                return false;
            }
        }
        return false;
    }
}

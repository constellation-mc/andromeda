package me.melontini.andromeda.config;

import me.melontini.andromeda.util.SharedConstants;
import me.melontini.dark_matter.api.base.util.PrependingLogger;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

public class AndromedaFeatureManager {
    private static final PrependingLogger LOGGER = new PrependingLogger(LogManager.getLogger("AndromedaFeatureManager"), PrependingLogger.LOGGER_NAME);
    private static final Map<String, FeatureProcessor> processors = new HashMap<>();
    private static final Map<Field, String> modifiedFields = new HashMap<>();

    private static void registerDefaultProcessors() {
        registerProcessor("connector_mod", config -> {
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
        registerProcessor("safe_beds_conflict", config -> {
            if (config.safeBeds) {
                return Map.of("bedsExplodeEverywhere", false);
            }
            if (config.bedsExplodeEverywhere) {
                return Map.of("safeBeds", false);
            }
            return null;
        });
        registerProcessor("iceberg", config -> {
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

    public static void registerProcessor(String feature, FeatureProcessor processor) {
        processors.putIfAbsent(feature, processor);
    }

    public static void unregisterProcessor(String feature) {
        processors.remove(feature);
    }

    public static boolean isModified(Field field) {
        return modifiedFields.containsKey(field);
    }

    public static String blameProcessor(Field field) {
        return modifiedFields.get(field);
    }

    public static void processFeatures(AndromedaConfig config) {
        if (!config.enableFeatureManager) return;
        modifiedFields.clear();

        for (Map.Entry<String, FeatureProcessor> entry : processors.entrySet()) {
            Map<String, Object> featureConfigEntry = entry.getValue().process(config);
            if (featureConfigEntry != null && !featureConfigEntry.isEmpty()) {
                LOGGER.info("Processor: {}", entry.getKey());
                StringBuilder builder = new StringBuilder();
                builder.append("Config: ");
                for (String s : featureConfigEntry.keySet()) {
                    builder.append(s).append("=").append(featureConfigEntry.get(s)).append("; ");
                }
                LOGGER.info(builder.toString());
                configure(config, entry.getKey(), featureConfigEntry);
            }
        }
    }

    private static void configure(AndromedaConfig config, String processor, Map<String, Object> featureConfig) {
        for (Map.Entry<String, Object> configEntry : featureConfig.entrySet()) {
            String configOption = configEntry.getKey();
            List<String> fields = Arrays.stream(configOption.split("\\.")).toList();

            try {
                if (fields.size() > 1) {//🤯🤯🤯
                    Object obj = config.getClass().getField(fields.get(0)).get(config);
                    for (int i = 1; i < fields.size() - 1; i++) {
                        obj = FieldUtils.readField(obj, fields.get(i), true);
                    }
                    Field field = obj.getClass().getField(fields.get(fields.size() - 1));
                    modifiedFields.put(field, processor);
                    FieldUtils.writeField(field, obj, configEntry.getValue());
                } else {
                    Field field = config.getClass().getField(configOption);
                    modifiedFields.put(field, processor);
                    FieldUtils.writeField(field, config, configEntry.getValue());
                }
            } catch (NoSuchFieldException e) {
                LOGGER.info("Invalid config option in AndromedaFeatureManager: " + configOption + " This is no fault of yours.");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public interface FeatureProcessor {
        @Nullable Map<String, Object> process(AndromedaConfig config);
    }

    static {
        registerDefaultProcessors();
    }

}

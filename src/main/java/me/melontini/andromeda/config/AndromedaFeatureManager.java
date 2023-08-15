package me.melontini.andromeda.config;

import me.melontini.andromeda.util.SharedConstants;
import me.melontini.dark_matter.api.base.util.PrependingLogger;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

public class AndromedaFeatureManager {
    private static final PrependingLogger LOGGER = new PrependingLogger(LogManager.getLogger("AndromedaFeatureManager"), PrependingLogger.LOGGER_NAME);
    private static final Map<String, FeatureProcessor> processors = new HashMap<>();
    private static final Set<Field> modifiedFields = new HashSet<>();

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
    }

    public static void registerProcessor(String feature, FeatureProcessor processor) {
        processors.putIfAbsent(feature, processor);
    }

    public static void unregisterProcessor(String feature) {
        processors.remove(feature);
    }

    public static boolean isModified(Field field) {
        return modifiedFields.contains(field);
    }

    public static void processFeatures(AndromedaConfig config) {
        if (!config.enableFeatureManager) return;
        modifiedFields.clear();

        Map<String, Object> featureConfig = new HashMap<>();
        for (Map.Entry<String, FeatureProcessor> entry : processors.entrySet()) {
            Map<String, Object> featureConfigEntry = entry.getValue().process(config);
            if (featureConfigEntry != null && !featureConfigEntry.isEmpty()) {
                featureConfig.putAll(featureConfigEntry);
            }
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : featureConfig.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
        }
        LOGGER.info("AndromedaFeatureManager: Setting config options:\n" + builder);

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
                    modifiedFields.add(field);
                    FieldUtils.writeField(field, obj, configEntry.getValue());
                } else {
                    Field field = config.getClass().getField(configOption);
                    modifiedFields.add(field);
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

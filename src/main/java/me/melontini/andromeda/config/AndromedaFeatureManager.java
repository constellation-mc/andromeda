package me.melontini.andromeda.config;

import me.melontini.dark_matter.api.base.util.PrependingLogger;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndromedaFeatureManager {
    private static final PrependingLogger LOGGER = new PrependingLogger(LogManager.getLogger("AndromedaFeatureManager"), PrependingLogger.LOGGER_NAME);
    private static final Map<String, FeatureProcessor> processors = new HashMap<>();
    private static final Map<Field, String> modifiedFields = new HashMap<>();

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
        modifiedFields.clear();
        if (!config.enableFeatureManager) return;

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
        FabricLoader.getInstance().getEntrypoints("andromeda:feature_manager", Runnable.class).forEach(Runnable::run);
    }

}

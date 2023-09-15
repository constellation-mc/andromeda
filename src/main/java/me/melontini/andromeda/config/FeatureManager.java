package me.melontini.andromeda.config;

import me.melontini.andromeda.api.config.ProcessorCollector;
import me.melontini.andromeda.api.config.ProcessorRegistry;
import me.melontini.andromeda.api.config.TranslatedEntry;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.base.util.PrependingLogger;
import me.melontini.dark_matter.api.base.util.classes.Tuple;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

public class FeatureManager {

    public static final String FEATURES_KEY = "andromeda:features";
    public static final String MIXIN_ERROR_ID = "andromeda:mixin_error";
    public static final String UNKNOWN_EXCEPTION_ID = "andromeda:unknown_exception";
    public static final String MOD_JSON_ID = "andromeda:mod_json";

    static final PrependingLogger LOGGER = PrependingLogger.get("FeatureManager");
    private static final Map<String, ProcessorEntry> PROCESSORS = new LinkedHashMap<>(5);

    static final Map<String, Set<String>> MOD_BLAME = new HashMap<>();
    static final Map<Field, Set<String>> MODIFIED_FIELDS = new HashMap<>();
    static final Map<Field, String> FIELD_TO_STRING = new HashMap<>();

    static final Map<String, Object> MOD_JSON = new LinkedHashMap<>();
    static final Map<String, MixinErrorEntry> FAILED_MIXINS = new HashMap<>();
    static final Map<String, ExceptionEntry> UNKNOWN_EXCEPTIONS = new HashMap<>();

    public static boolean isModified(Field field) {
        return MODIFIED_FIELDS.containsKey(field);
    }

    public static Set<Tuple<ProcessorEntry, String>> blameProcessors(Field field) {
        Set<Tuple<ProcessorEntry, String>> processors = new HashSet<>();
        for (String s : MODIFIED_FIELDS.get(field)) {
            processors.add(Tuple.of(PROCESSORS.get(s), FIELD_TO_STRING.get(field)));
        }
        return processors;
    }

    public static void processMixinError(String feature, String className) {
        FAILED_MIXINS.put(feature, new MixinErrorEntry(feature, false, className));
        configure(MIXIN_ERROR_ID, Collections.singletonMap(feature, false));
        ConfigHelper.writeConfigToFile(false);
    }

    public static void processUnknownException(Throwable t, String... features) {
        for (String feature : features) {
            UNKNOWN_EXCEPTIONS.put(feature, new ExceptionEntry(feature, false, t));
            configure(UNKNOWN_EXCEPTION_ID, Collections.singletonMap(feature, false));
        }
        ConfigHelper.writeConfigToFile(false);
    }

    public static void processFeatures(boolean print) {
        MODIFIED_FIELDS.clear();
        if (!Config.get().enableFeatureManager) return;

        PROCESSORS.forEach((key, entry) -> {
            var config = entry.processor().apply(Config.get());
            if (config != null && !config.isEmpty()) {
                if (print) {
                    LOGGER.info("Processor: {}", key);
                    StringBuilder builder = new StringBuilder().append("Config: ");
                    config.keySet().forEach(s -> builder.append(s).append("=").append(config.get(s)).append("; "));
                    LOGGER.info(builder.toString());
                }

                configure(key, config);
            }
        });
    }

    private static void configure(String processor, Map<String, Object> featureConfig) {
        validateId(processor);

        Set<String> skipped = new HashSet<>();
        featureConfig.forEach((feature, value) -> {
            try {
                Field f = Config.set(feature, value);
                MODIFIED_FIELDS.computeIfAbsent(f, k -> new HashSet<>()).add(processor);
                FIELD_TO_STRING.putIfAbsent(f, feature);
            } catch (NoSuchFieldException e) {
                skipped.add(feature);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        if (!skipped.isEmpty()) {
            StringBuilder builder = new StringBuilder().append("Skipped: ");
            skipped.forEach(s -> builder.append(s).append("; "));
            LOGGER.warn(builder);
        }
    }

    static {
        ProcessorRegistry registry = new ProcessorRegistry() {
            @Override
            public void register(String id, Function<AndromedaConfig, Map<String, Object>> processor) {
                validateId(id);
                var last = PROCESSORS.put(id, ProcessorEntry.of(id, processor));
                if (last != null) throw new IllegalArgumentException("Duplicate processor: " + id);
            }

            @Override
            public void register(String id, Function<AndromedaConfig, Map<String, Object>> processor, Function<String, TranslatedEntry> reason) {
                validateId(id);
                var last = PROCESSORS.put(id, ProcessorEntry.of(id, processor, reason));
                if (last != null) throw new IllegalArgumentException("Duplicate processor: " + id);
            }

            @Override
            public <T> @Nullable T get(String feature) {
                try {
                    return Config.get(feature);
                } catch (NoSuchFieldException e) {
                    return null;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        };

        SpecialProcessors.init(registry);

        EntrypointRunner.runEntrypoint(FEATURES_KEY, ProcessorCollector.class, collector -> collector.collect(registry));
        EntrypointRunner.runEntrypoint("andromeda:feature_manager", Runnable.class, Runnable::run);
    }

    static void validateId(String id) {
        String[] split = id.split(":");
        if (split.length != 2) throw new IllegalArgumentException("Invalid id: " + id);
    }

    static void legacyRegister(String id, Function<AndromedaConfig, Map<String, Object>> processor) {
        String newId = id.split(":").length == 2 ? id : "legacy:" + id;
        PROCESSORS.putIfAbsent(newId, ProcessorEntry.of(newId, processor, feature -> TranslatedEntry.withPrefix(id)));
    }

    record MixinErrorEntry(String feature, Object value, String className) {
    }

    record ExceptionEntry(String feature, Object value, Throwable cause) {
    }

    public record ProcessorEntry(String id, Function<AndromedaConfig, Map<String, Object>> processor, Function<String, TranslatedEntry> reason) {

        public static ProcessorEntry of(String id, Function<AndromedaConfig, Map<String, Object>> processor) {
            return new ProcessorEntry(id, processor, feature -> TranslatedEntry.withPrefix(id.replace(":", ".")));
        }

        public static ProcessorEntry of(String id, Function<AndromedaConfig, Map<String, Object>> processor, Function<String, TranslatedEntry> reason) {
            return new ProcessorEntry(id, processor, reason);
        }
    }
}

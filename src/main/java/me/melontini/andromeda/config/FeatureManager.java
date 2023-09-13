package me.melontini.andromeda.config;

import me.melontini.andromeda.api.FeatureConfig.Processor;
import me.melontini.andromeda.api.FeatureConfig.ReasonSupplier;
import me.melontini.andromeda.api.FeatureConfig.TranslatedEntry;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.base.util.PrependingLogger;
import me.melontini.dark_matter.api.base.util.classes.Tuple;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

import java.lang.reflect.Field;
import java.util.*;

public class FeatureManager {

    static final PrependingLogger LOGGER = PrependingLogger.get("FeatureManager");
    private static final Map<String, ProcessorEntry> PROCESSORS = new LinkedHashMap<>(5);

    static final Map<String, Set<String>> MOD_BLAME = new HashMap<>();
    static final Map<Field, Set<String>> MODIFIED_FIELDS = new HashMap<>();
    static final Map<Field, String> FIELD_TO_STRING = new HashMap<>();

    static final Map<String, Object> MOD_JSON = new LinkedHashMap<>();
    static final Map<String, FeatureManager.MixinErrorEntry> FAILED_MIXINS = new HashMap<>();
    static final Map<String, FeatureManager.ExceptionEntry> UNKNOWN_EXCEPTIONS = new HashMap<>();

    public static void registerProcessor(String id, Processor processor) {
        PROCESSORS.putIfAbsent(id, new ProcessorEntry(id, processor));
    }

    public static void registerProcessor(String id, Processor processor, ReasonSupplier reasonSupplier) {
        PROCESSORS.putIfAbsent(id, new ProcessorEntry(id, processor, reasonSupplier));
    }

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

    public static void processMixinError(String option, String className) {
        FAILED_MIXINS.put(option, new MixinErrorEntry(option, false, className));
        configure("mixin_error", Collections.singletonMap(option, false));
        ConfigHelper.writeConfigToFile(false);
    }

    public static void processUnknownException(Throwable t, String... option) {
        for (String s : option) {
            UNKNOWN_EXCEPTIONS.put(s, new ExceptionEntry(s, false, t));
            configure("unknown_exception", Collections.singletonMap(s, false));
        }
        ConfigHelper.writeConfigToFile(false);
    }

    public static void processFeatures(boolean print) {
        MODIFIED_FIELDS.clear();
        if (!Config.get().enableFeatureManager) return;

        for (Map.Entry<String, ProcessorEntry> entry : PROCESSORS.entrySet()) {
            Map<String, Object> featureConfigEntry = entry.getValue().processor().process(Config.get());
            if (featureConfigEntry != null && !featureConfigEntry.isEmpty()) {

                if (print) {
                    LOGGER.info("Processor: {}", entry.getKey());
                    StringBuilder builder = new StringBuilder().append("Config: ");
                    featureConfigEntry.keySet().forEach(s -> builder.append(s).append("=").append(featureConfigEntry.get(s)).append("; "));
                    LOGGER.info(builder.toString());
                }

                configure(entry.getKey(), featureConfigEntry);
            }
        }
    }

    private static void configure(String processor, Map<String, Object> featureConfig) {
        Set<String> skipped = new HashSet<>();
        for (Map.Entry<String, Object> configEntry : featureConfig.entrySet()) {
            String configOption = configEntry.getKey();
            try {
                Field f = ConfigHelper.setConfigOption(configOption, configEntry.getValue());
                MODIFIED_FIELDS.computeIfAbsent(f, k -> new HashSet<>()).add(processor);
                FIELD_TO_STRING.putIfAbsent(f, configOption);
            } catch (NoSuchFieldException e) {
                skipped.add(configOption);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        if (!skipped.isEmpty()) {
            StringBuilder builder = new StringBuilder().append("Skipped: ");
            skipped.forEach(s -> builder.append(s).append("; "));
            LOGGER.warn(builder);
        }
    }

    static {
        SpecialProcessors.init();

        EntrypointRunner.runEntrypoint("andromeda:feature_manager", Runnable.class, Runnable::run);
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

    record MixinErrorEntry(String option, Object value, String className) { }

    record ExceptionEntry(String option, Object value, Throwable cause) { }

    public record ProcessorEntry(String id, Processor processor, ReasonSupplier reasonSupplier) {

        public ProcessorEntry(String id, Processor processor) {
            this(id, processor, config -> TranslatedEntry.of("andromeda.config.tooltip.manager." + id));
        }
    }
}

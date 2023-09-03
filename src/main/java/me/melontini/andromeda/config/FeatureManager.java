package me.melontini.andromeda.config;

import me.melontini.andromeda.api.FeatureConfig;
import me.melontini.andromeda.util.ConfigHelper;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.base.util.PrependingLogger;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Field;
import java.util.*;

public class FeatureManager {

    static final PrependingLogger LOGGER = new PrependingLogger(LogManager.getLogger("FeatureManager"), PrependingLogger.LOGGER_NAME);
    private static final Map<String, FeatureConfig.Processor> PROCESSORS = new LinkedHashMap<>(5);
    private static final Map<String, Set<String>> MOD_BLAME = new HashMap<>();
    private static final Map<Field, Set<String>> MODIFIED_FIELDS = new HashMap<>();
    private static final Map<Field, String> FIELD_TO_STRING = new HashMap<>();
    private static final Map<String, Object> MOD_JSON = new LinkedHashMap<>();
    private static final Set<String> FAILED_MIXINS = new HashSet<>();


    public static void registerProcessor(String id, FeatureConfig.Processor processor) {
        PROCESSORS.putIfAbsent(id, processor);
    }

    public static void unregisterProcessor(String id) {
        PROCESSORS.remove(id);
    }

    public static boolean isModified(Field field) {
        return MODIFIED_FIELDS.containsKey(field);
    }

    public static Set<String> blameProcessors(Field field) {
        return MODIFIED_FIELDS.get(field);
    }

    public static String[] blameMod(Field feature) {
        return MOD_BLAME.get(FIELD_TO_STRING.get(feature)).stream().sorted(String::compareToIgnoreCase).toArray(String[]::new);
    }

    public static void processMixinError(String option) {
        FAILED_MIXINS.add(option);
        configure("mixin_error", Collections.singletonMap(option, false));
    }

    public static void processFeatures(boolean print) {
        MODIFIED_FIELDS.clear();
        if (!Config.get().enableFeatureManager) return;

        for (Map.Entry<String, FeatureConfig.Processor> entry : PROCESSORS.entrySet()) {
            Map<String, Object> featureConfigEntry = entry.getValue().process(Config.get());
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
        //This needs to be here to interact with private fields.
        FeatureManager.registerProcessor("mod_json", config -> {
            if (MOD_JSON.isEmpty()) FabricLoader.getInstance().getAllMods().stream()
                    .filter(mod -> mod.getMetadata().containsCustomValue("andromeda:features"))
                    .forEach(FeatureManager::parseMetadata);
            return MOD_JSON;
        });
        FeatureManager.registerProcessor("mixin_error", config -> {
            if (FAILED_MIXINS.isEmpty()) return null;

            Map<String, Object> map = new LinkedHashMap<>();
            for (String failedMixin : FAILED_MIXINS) {
                map.put(failedMixin, false);
            }
            return map;
        });
        EntrypointRunner.runEntrypoint("andromeda:feature_manager", Runnable.class, Runnable::run);
    }

    private static void parseMetadata(ModContainer mod) {
        CustomValue customValue = mod.getMetadata().getCustomValue("andromeda:features");
        if (customValue.getType() != CustomValue.CvType.OBJECT)
            LOGGER.error("andromeda:features must be an object. Mod: " + mod.getMetadata().getId() + " Type: " + customValue.getType());
        else {
            CustomValue.CvObject object = customValue.getAsObject();
            for (Map.Entry<String, CustomValue> feature : object) {
                switch (feature.getValue().getType()) {
                    case BOOLEAN -> {
                        MOD_JSON.put(feature.getKey(), feature.getValue().getAsBoolean());
                        MOD_BLAME.computeIfAbsent(feature.getKey(), k -> new HashSet<>()).add(mod.getMetadata().getName());
                    }
                    case OBJECT -> {
                        CustomValue.CvObject featureObject = feature.getValue().getAsObject();
                        if (!featureObject.containsKey("value")) {
                            LOGGER.error("Missing \"value\" field in andromeda:features. Mod: " + mod.getMetadata().getId());
                            continue;
                        }
                        if (!testModVersion(featureObject, "minecraft", feature.getKey())) continue;
                        if (!testModVersion(featureObject, "andromeda", feature.getKey())) continue;
                        if (featureObject.get("value").getType() == CustomValue.CvType.BOOLEAN) {
                            MOD_JSON.put(feature.getKey(), featureObject.get("value").getAsBoolean());
                            MOD_BLAME.computeIfAbsent(feature.getKey(), k -> new HashSet<>()).add(mod.getMetadata().getName());
                        } else
                            LOGGER.error("Unsupported andromeda:features type. Mod: " + mod.getMetadata().getId() + " Type: " + feature.getValue().getType());
                    }
                    default ->
                            LOGGER.error("Unsupported andromeda:features type. Mod: " + mod.getMetadata().getId() + " Type: " + feature.getValue().getType());
                }
            }
        }
    }

    private static boolean testModVersion(CustomValue.CvObject featureObject, String modId, String modBlame) {
        if (featureObject.containsKey(modId)) {
            try {
                VersionPredicate predicate = VersionPredicate.parse(featureObject.get(modId).getAsString());
                return predicate.test(FabricLoader.getInstance().getModContainer(modId).orElseThrow().getMetadata().getVersion());
            } catch (VersionParsingException e) {
                LOGGER.error("Couldn't parse version predicate for {} provided by {}", modId, modBlame);
                return false;
            }
        }
        return true;
    }
}

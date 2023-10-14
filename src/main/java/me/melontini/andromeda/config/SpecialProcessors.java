package me.melontini.andromeda.config;

import lombok.CustomLog;
import me.melontini.dark_matter.api.config.OptionProcessorRegistry;
import me.melontini.dark_matter.api.config.interfaces.TextEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.CustomValue.CvObject;
import net.fabricmc.loader.api.metadata.CustomValue.CvType;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

import java.util.*;

import static me.melontini.andromeda.config.Config.DEFAULT_KEY;


@SuppressWarnings("UnstableApiUsage")
@CustomLog
class SpecialProcessors {

    public static final String MIXIN_ERROR_ID = "andromeda:mixin_error";
    public static final String UNKNOWN_EXCEPTION_ID = "andromeda:unknown_exception";
    public static final String MOD_JSON_ID = "andromeda:mod_json";
    public static final String FEATURES_KEY = "andromeda:features";

    static final Map<String, Set<String>> MOD_BLAME = new HashMap<>();
    static final Map<String, Object> MOD_JSON = new LinkedHashMap<>();
    static final Map<String, MixinErrorEntry> FAILED_MIXINS = new HashMap<>();
    static final Map<String, ExceptionEntry> UNKNOWN_EXCEPTIONS = new HashMap<>();

    static void collect(OptionProcessorRegistry<AndromedaConfig> registry) {
        registry.register(MOD_JSON_ID, config -> {
            if (MOD_JSON.isEmpty()) FabricLoader.getInstance().getAllMods().stream()
                    .filter(mod -> mod.getMetadata().containsCustomValue(FEATURES_KEY))
                    .forEach(SpecialProcessors::parseMetadata);
            return MOD_JSON;
        }, (holder) -> TextEntry.translatable(DEFAULT_KEY + "mod_json", Arrays.toString(MOD_BLAME.get(holder.option()).toArray())));

        registry.register(MIXIN_ERROR_ID, config -> {
            if (!FAILED_MIXINS.isEmpty()) {
                Map<String, Object> map = new LinkedHashMap<>();
                FAILED_MIXINS.forEach((k, v) -> map.put(k, v.value()));
                map.put("compatMode", true);
                return map;
            }
            return null;
        }, (holder) -> {
            MixinErrorEntry entry = FAILED_MIXINS.get(holder.option());
            String[] split = entry.className().split("\\.");
            return TextEntry.translatable(DEFAULT_KEY + "mixin_error", split[split.length - 1]);
        });

        registry.register(UNKNOWN_EXCEPTION_ID, config -> {
            if (!UNKNOWN_EXCEPTIONS.isEmpty()) {
                Map<String, Object> map = new LinkedHashMap<>();
                UNKNOWN_EXCEPTIONS.forEach((k, v) -> map.put(k, v.value()));
                map.put("compatMode", true);
                return map;
            }
            return null;
        }, (holder) -> {
            ExceptionEntry entry = UNKNOWN_EXCEPTIONS.get(holder.option());
            Throwable cause = entry.cause();
            if (cause.getLocalizedMessage() != null) {
                return TextEntry.translatable(DEFAULT_KEY + "unknown_exception[1]", cause.getClass().getSimpleName(), cause.getLocalizedMessage());
            }
            return TextEntry.translatable(DEFAULT_KEY + "unknown_exception[0]", cause.getClass().getSimpleName());
        });
    }

    private static void parseMetadata(ModContainer mod) {
        CustomValue customValue = mod.getMetadata().getCustomValue(FEATURES_KEY);
        if (customValue.getType() != CvType.OBJECT)
            LOGGER.error("{} must be an object. Mod: {} Type: {}", FEATURES_KEY, mod.getMetadata().getId(), customValue.getType());
        else {
            CvObject object = customValue.getAsObject();
            for (Map.Entry<String, CustomValue> feature : object) {
                switch (feature.getValue().getType()) {
                    case BOOLEAN -> addModJson(mod, feature.getKey(), feature.getValue().getAsBoolean());
                    case OBJECT -> parseFeatureObject(feature.getValue().getAsObject(), mod, feature.getKey());
                    default ->
                            LOGGER.error("Unsupported {} type. Mod: {}, Type: {}", FEATURES_KEY, mod.getMetadata().getId(), feature.getValue().getType());
                }
            }
        }
    }

    private static void parseFeatureObject(CvObject featureObject, ModContainer mod, String feature) {
        if (!featureObject.containsKey("value")) {
            LOGGER.error("Missing \"value\" field in {}. Mod: {}", FEATURES_KEY, mod.getMetadata().getId());
            return;
        }
        if (!testModVersion(featureObject, "minecraft", feature)) return;
        if (!testModVersion(featureObject, "andromeda", feature)) return;

        if (featureObject.get("value").getType() == CvType.BOOLEAN) {
            addModJson(mod, feature, featureObject.get("value").getAsBoolean());
        } else
            LOGGER.error("Unsupported {} type. Mod: {}, Type: {}", FEATURES_KEY, mod.getMetadata().getId(), featureObject.get("value").getType());
    }

    private static void addModJson(ModContainer mod, String feature, Object value) {
        MOD_JSON.put(feature, value);
        MOD_BLAME.computeIfAbsent(feature, k -> new LinkedHashSet<>()).add(mod.getMetadata().getName());
    }

    static boolean testModVersion(CvObject featureObject, String modId, String modBlame) {
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

    record MixinErrorEntry(String feature, Object value, String className) {
    }

    record ExceptionEntry(String feature, Object value, Throwable cause) {
    }
}

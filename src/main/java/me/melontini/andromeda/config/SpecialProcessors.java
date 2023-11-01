package me.melontini.andromeda.config;

import com.google.gson.*;
import lombok.*;
import lombok.experimental.Accessors;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.config.OptionProcessorRegistry;
import me.melontini.dark_matter.api.config.interfaces.TextEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.CustomValue.CvObject;
import net.fabricmc.loader.api.metadata.CustomValue.CvType;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
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
        }, CommonValues.mod(), (holder) -> TextEntry.translatable(DEFAULT_KEY + "mod_json", Arrays.toString(MOD_BLAME.get(holder.option()).toArray())));

        registry.register(MIXIN_ERROR_ID, config -> {
            if (!FAILED_MIXINS.isEmpty()) {
                Map<String, Object> map = new LinkedHashMap<>();
                FAILED_MIXINS.forEach((k, v) -> map.put(k, v.value()));
                map.put("compatMode", true);
                return map;
            }
            return null;
        }, CommonValues.mod(), (holder) -> {
            MixinErrorEntry entry = FAILED_MIXINS.get(holder.option());
            if (entry == null) return null;

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
        }, CommonValues.mod(), (holder) -> {
            ExceptionEntry entry = UNKNOWN_EXCEPTIONS.get(holder.option());
            if (entry == null) return null;

            if (entry.message() != null) {
                return TextEntry.translatable(DEFAULT_KEY + "unknown_exception[1]", entry.errorClass(), entry.message());
            }
            return TextEntry.translatable(DEFAULT_KEY + "unknown_exception[0]", entry.errorClass());
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

    private static final Gson GSON = new Gson();
    private static final Path SAVE_PATH = CommonValues.hiddenPath().resolve("disabled_options.json");

    static {
        if (Files.exists(SAVE_PATH)) parseFromJson(Utilities.supplyUnchecked(() -> Files.newBufferedReader(SAVE_PATH)));
    }

    static void saveToJson() {
        JsonObject object = new JsonObject();
        if (!FAILED_MIXINS.isEmpty()) {
            JsonArray array = new JsonArray();
            FAILED_MIXINS.forEach((s, mixinErrorEntry) -> array.add(GSON.toJsonTree(mixinErrorEntry)));
            object.add("mixins", array);
        }
        if (!UNKNOWN_EXCEPTIONS.isEmpty()) {
            JsonArray array = new JsonArray();
            UNKNOWN_EXCEPTIONS.forEach((s, exceptionEntry) -> array.add(GSON.toJsonTree(exceptionEntry)));
            object.add("exceptions", array);
        }
        try {
            Files.writeString(SAVE_PATH, GSON.toJson(object));
        } catch (Exception e) {
            LOGGER.error("Failed to write disabled options to file", e);
        }
    }

    private static void parseFromJson(Reader reader) {
        if (CommonValues.updated()) {
            LOGGER.info("Mod updated! Removing old exceptions.");
            Utilities.runUnchecked(() -> Files.deleteIfExists(SAVE_PATH));
            return;
        }

        LOGGER.info("Loading exceptions from json!");
        JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
        if (object.has("mixins")) {
            JsonArray array = object.getAsJsonArray("mixins");
            for (JsonElement element : array) {
                FAILED_MIXINS.put(element.getAsJsonObject().get("feature").getAsString(), SpecialProcessors.GSON.fromJson(element, MixinErrorEntry.class));
            }
        }
        if (object.has("exceptions")) {
            JsonArray array = object.getAsJsonArray("exceptions");
            for (JsonElement element : array) {
                UNKNOWN_EXCEPTIONS.put(element.getAsJsonObject().get("feature").getAsString(), SpecialProcessors.GSON.fromJson(element, ExceptionEntry.class));
            }
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    @Getter @Accessors(fluent = true)
    @ToString @EqualsAndHashCode @AllArgsConstructor
    static final class MixinErrorEntry {
        private final String feature;
        private final Object value;
        private final String className;
    }

    @SuppressWarnings("ClassCanBeRecord")
    @Getter @Accessors(fluent = true)
    @ToString @EqualsAndHashCode @AllArgsConstructor
    static final class ExceptionEntry {
        private final String feature;
        private final Object value;
        private final String errorClass;
        private final String message;
    }
}

package me.melontini.andromeda.config;

import me.melontini.andromeda.api.FeatureConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static me.melontini.andromeda.config.FeatureManager.*;

class SpecialProcessors {

    static void init() {
        FeatureManager.registerProcessor("mod_json", config -> {
            if (MOD_JSON.isEmpty()) FabricLoader.getInstance().getAllMods().stream()
                    .filter(mod -> mod.getMetadata().containsCustomValue("andromeda:features"))
                    .forEach(SpecialProcessors::parseMetadata);
            return MOD_JSON;
        }, option -> FeatureConfig.TranslatedEntry.of("andromeda.config.tooltip.manager.mod_json", Arrays.toString(MOD_BLAME.get(option).toArray())));

        FeatureManager.registerProcessor("mixin_error", config -> {
            Map<String, Object> map = new LinkedHashMap<>();
            FAILED_MIXINS.forEach((k, v) -> map.put(k, v.value()));
            return map;
        }, option -> {
            FeatureManager.MixinErrorEntry entry = FAILED_MIXINS.get(option);
            String[] split = entry.className().split("\\.");
            return FeatureConfig.TranslatedEntry.of("andromeda.config.tooltip.manager.mixin_error", split[split.length - 1]);
        });

        FeatureManager.registerProcessor("unknown_exception", config -> {
            Map<String, Object> map = new LinkedHashMap<>();
            UNKNOWN_EXCEPTIONS.forEach((k, v) -> map.put(k, v.value()));
            return map;
        }, option -> {
            FeatureManager.ExceptionEntry entry = UNKNOWN_EXCEPTIONS.get(option);
            if (entry.cause().getLocalizedMessage() != null) {
                return FeatureConfig.TranslatedEntry.of("andromeda.config.tooltip.manager.unknown_exception[1]", entry.cause().getClass().getSimpleName(), entry.cause().getLocalizedMessage());
            }
            return FeatureConfig.TranslatedEntry.of("andromeda.config.tooltip.manager.unknown_exception[0]", entry.cause().getClass().getSimpleName());
        });
    }

    private static void parseMetadata(ModContainer mod) {
        CustomValue customValue = mod.getMetadata().getCustomValue("andromeda:features");
        if (customValue.getType() != CustomValue.CvType.OBJECT)
            LOGGER.error("andromeda:features must be an object. Mod: " + mod.getMetadata().getId() + " Type: " + customValue.getType());
        else {
            CustomValue.CvObject object = customValue.getAsObject();
            for (Map.Entry<String, CustomValue> feature : object) {
                switch (feature.getValue().getType()) {
                    case BOOLEAN -> addModJson(mod, feature.getKey(), feature.getValue().getAsBoolean());
                    case OBJECT -> parseFeatureObject(feature.getValue().getAsObject(), mod, feature.getKey());
                    default ->
                            LOGGER.error("Unsupported andromeda:features type. Mod: " + mod.getMetadata().getId() + " Type: " + feature.getValue().getType());
                }
            }
        }
    }

    private static void parseFeatureObject(CustomValue.CvObject featureObject, ModContainer mod, String option) {
        if (!featureObject.containsKey("value")) {
            LOGGER.error("Missing \"value\" field in andromeda:features. Mod: " + mod.getMetadata().getId());
            return;
        }
        if (!testModVersion(featureObject, "minecraft", option)) return;
        if (!testModVersion(featureObject, "andromeda", option)) return;

        if (featureObject.get("value").getType() == CustomValue.CvType.BOOLEAN) {
            addModJson(mod, option, featureObject.get("value").getAsBoolean());
        } else
            LOGGER.error("Unsupported andromeda:features type. Mod: " + mod.getMetadata().getId() + " Type: " + featureObject.get("value").getType());
    }

    private static void addModJson(ModContainer mod, String option, Object value) {
        MOD_JSON.put(option, value);
        MOD_BLAME.computeIfAbsent(option, k -> new LinkedHashSet<>()).add(mod.getMetadata().getName());
    }

    static boolean testModVersion(CustomValue.CvObject featureObject, String modId, String modBlame) {
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

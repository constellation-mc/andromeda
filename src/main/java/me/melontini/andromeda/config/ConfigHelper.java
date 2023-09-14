package me.melontini.andromeda.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.SharedConstants;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.base.util.classes.ThrowingRunnable;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static me.melontini.dark_matter.api.base.util.Utilities.cast;

public class ConfigHelper {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, String> REDIRECTS = Utilities.consume(new HashMap<>(), map -> {
        map.put("throwableItems", "throwableItems.enable");//Since throwableItems is now an object, we can assume that whatever this is wants a boolean.
        map.put("throwableItemsBlacklist", "throwableItems.blacklist");

        map.put("incubatorSettings.enableIncubator", "incubator.enable");
        map.put("incubatorSettings.incubatorRandomness", "incubator.randomness");
        map.put("incubatorSettings.incubatorRecipe", "incubator.recipe");

        map.put("autogenRecipeAdvancements.autogenRecipeAdvancements", "recipeAdvancementsGeneration.enable");
        map.put("autogenRecipeAdvancements.blacklistedRecipeNamespaces", "recipeAdvancementsGeneration.namespaceBlacklist");
        map.put("autogenRecipeAdvancements.blacklistedRecipeIds", "recipeAdvancementsGeneration.recipeBlacklist");

        map.put("campfireTweaks.campfireEffects", "campfireTweaks.effects");
        map.put("campfireTweaks.campfireEffectsPassive", "campfireTweaks.affectsPassive");
        map.put("campfireTweaks.campfireEffectsRange", "campfireTweaks.effectsRange");
    });

    public static String redirect(String s) {
        return REDIRECTS.getOrDefault(s, s);
    }

    public static Field set(String feature, final Object value) throws NoSuchFieldException, IllegalAccessException {
        if ((feature = redirect(feature)).contains(".")) {
            String[] fields = feature.split("\\.");
            Object obj = Config.get().getClass().getField(fields[0]).get(Config.get());
            for (int i = 1; i < fields.length - 1; i++) {
                obj = FieldUtils.readField(obj, fields[i], true);
            }
            Field field = obj.getClass().getField(fields[fields.length - 1]);
            FieldUtils.writeField(field, obj, value);
            return field;
        } else {
            Field field = Config.get().getClass().getField(feature);
            FieldUtils.writeField(field, Config.get(), value);
            return field;
        }
    }


    public static <T> T get(String feature) throws NoSuchFieldException, IllegalAccessException {
        if ((feature = redirect(feature)).contains(".")) {
            String[] fields = feature.split("\\.");
            Object obj = Config.get().getClass().getField(fields[0]).get(Config.get());
            for (int i = 1; i < fields.length - 1; i++) {
                obj = FieldUtils.readField(obj, fields[i], true);
            }
            return cast(FieldUtils.readField(obj, fields[fields.length - 1], true));
        } else {
            return cast(Config.get().getClass().getField(feature).get(Config.get()));
        }
    }

    public static void writeConfigToFile(boolean print) {
        Path configPath = SharedConstants.CONFIG_PATH;
        try {
            FeatureManager.processFeatures(print);
            Files.write(configPath, gson.toJson(Config.get()).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadConfigFromFile(boolean print) {
        Path configPath = SharedConstants.CONFIG_PATH;
        if (Files.exists(configPath)) {
            try (var reader = Files.newBufferedReader(configPath)) {
                JsonObject object = Fixup.fixup(JsonParser.parseReader(reader).getAsJsonObject());

                Config.set(gson.fromJson(object, AndromedaConfig.class));
                writeConfigToFile(print);
                return;
            } catch (Exception e) {
                AndromedaLog.error("Failed to load config file, resetting to default!", e);
            }
        }
        Config.set(new AndromedaConfig());
        writeConfigToFile(print);
    }

    public static void run(ThrowingRunnable runnable, String... features) {
        try {
            runnable.run();
        } catch (Throwable e) {
            AndromedaLog.error("Something went very wrong! Disabling %s".formatted(Arrays.toString(features)), e);
            FeatureManager.processUnknownException(e, features);
        }
    }

    public static <T> T run(Callable<T> callable, String... features) {
        try {
            return callable.call();
        } catch (Throwable e) {
            AndromedaLog.error("Something went very wrong! Disabling %s".formatted(Arrays.toString(features)), e);
            FeatureManager.processUnknownException(e, features);
            return null;
        }
    }
}

package me.melontini.andromeda.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.SharedConstants;
import me.melontini.andromeda.util.ThrowingRunnable;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class ConfigHelper {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Field setConfigOption(String configOption, Object value) throws NoSuchFieldException, IllegalAccessException {
        if (configOption.contains(".")) {
            String[] fields = configOption.split("\\.");
            Object obj = Config.get().getClass().getField(fields[0]).get(Config.get());
            for (int i = 1; i < fields.length - 1; i++) {
                obj = FieldUtils.readField(obj, fields[i], true);
            }
            Field field = obj.getClass().getField(fields[fields.length - 1]);
            FieldUtils.writeField(field, obj, value);
            return field;
        } else {
            Field field = Config.get().getClass().getField(configOption);
            FieldUtils.writeField(field, Config.get(), value);
            return field;
        }
    }


    public static Object getConfigOption(String configOption) throws NoSuchFieldException, IllegalAccessException {
        if (configOption.contains(".")) {
            String[] fields = configOption.split("\\.");
            Object obj = Config.get().getClass().getField(fields[0]).get(Config.get());
            for (int i = 1; i < fields.length - 1; i++) {
                obj = FieldUtils.readField(obj, fields[i], true);
            }
            return FieldUtils.readField(obj, fields[fields.length - 1], true);
        } else {
            return Config.get().getClass().getField(configOption).get(Config.get());
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
            try(var reader = Files.newBufferedReader(configPath)) {
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

    public static void run(ThrowingRunnable runnable, String... optionsToDisable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            AndromedaLog.error("Something went very wrong! Disabling %s".formatted(Arrays.toString(optionsToDisable)), e);
            FeatureManager.processUnknownException(optionsToDisable);
        }
    }

    public static <T> T run(Callable<T> callable, String... optionsToDisable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            AndromedaLog.error("Something went very wrong! Disabling %s".formatted(Arrays.toString(optionsToDisable)), e);
            FeatureManager.processUnknownException(optionsToDisable);
            return null;
        }
    }

}

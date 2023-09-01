package me.melontini.andromeda.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.config.FeatureManager;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHelper {

    public static Field setConfigOption(String configOption, Object config, Object value) throws NoSuchFieldException, IllegalAccessException {
        if (configOption.contains(".")) {
            String[] fields = configOption.split("\\.");
            Object obj = config.getClass().getField(fields[0]).get(config);
            for (int i = 1; i < fields.length - 1; i++) {
                obj = FieldUtils.readField(obj, fields[i], true);
            }
            Field field = obj.getClass().getField(fields[fields.length - 1]);
            FieldUtils.writeField(field, obj, value);
            return field;
        } else {
            Field field = config.getClass().getField(configOption);
            FieldUtils.writeField(field, config, value);
            return field;
        }
    }


    public static Object getConfigOption(String configOption, Object config) throws NoSuchFieldException, IllegalAccessException {
        if (configOption.contains(".")) {
            String[] fields = configOption.split("\\.");
            Object obj = config.getClass().getField(fields[0]).get(config);
            for (int i = 1; i < fields.length - 1; i++) {
                obj = FieldUtils.readField(obj, fields[i], true);
            }
            return FieldUtils.readField(obj, fields[fields.length - 1], true);
        } else {
            return config.getClass().getField(configOption).get(config);
        }
    }

    public static AndromedaConfig loadConfigFromFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path configPath = SharedConstants.CONFIG_PATH;
        AndromedaConfig config;
        if (Files.exists(configPath)) {
            try {
                config = gson.fromJson(Files.readString(configPath), AndromedaConfig.class);
                FeatureManager.processFeatures(config);
                Files.write(configPath, gson.toJson(config).getBytes());
                return config;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            config = new AndromedaConfig();
            FeatureManager.processFeatures(config);
            try {
                Files.createFile(configPath);
                Files.write(configPath, gson.toJson(config).getBytes());
                return config;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

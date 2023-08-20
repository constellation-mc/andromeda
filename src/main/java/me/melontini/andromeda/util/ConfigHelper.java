package me.melontini.andromeda.util;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;

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

}

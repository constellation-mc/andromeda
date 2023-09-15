package me.melontini.andromeda.config;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Utilities;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class Config {

    private static AndromedaConfig CONFIG;

    private static final Map<String, Field> STRING_TO_FIELD = Maps.newLinkedHashMap();
    private static final Map<String, String> FIELD_TO_STRING = Maps.newHashMap();

    private static final Set<String> BLOCKED = Sets.newHashSet();
    private static final Map<String, Field> FILTERED = Maps.newLinkedHashMap();

    @SuppressWarnings("FieldMayBeFinal")
    private static Getter getterFunc = ConfigHelper::get;
    @SuppressWarnings("FieldMayBeFinal")
    private static Setter setterFunc = ConfigHelper::set;

    static void set(AndromedaConfig config) {
        MakeSure.notNull(config, "Tried to nullify config");
        MakeSure.isTrue(CONFIG == null, "Tried to set config twice");
        CONFIG = config;

        scanClass();
    }

    private static void scanClass() {
        clear(STRING_TO_FIELD, FIELD_TO_STRING);
        BLOCKED.clear();
        Set<Class<?>> allowedClasses = Sets.newHashSet(AndromedaConfig.class.getClasses());

        iterate(AndromedaConfig.class, get(), "", allowedClasses);

        FILTERED.putAll(STRING_TO_FIELD);
        for (String s : BLOCKED) {
            FILTERED.remove(s);
        }
    }

    static void clear(Map<?,?>... maps) {
        for (Map<?,?> map : maps) {
            map.clear();
        }
    }

    @SneakyThrows
    private static void iterate(Class<?> cls, Object parent, String parentString, Set<Class<?>> recursive) {
        for (Field declaredField : cls.getDeclaredFields()) {
            STRING_TO_FIELD.putIfAbsent(parentString + declaredField.getName(), declaredField);
            FIELD_TO_STRING.putIfAbsent(declaredField.getName(), parentString + declaredField.getName());

            if (recursive.contains(declaredField.getType())) {
                BLOCKED.add(parentString + declaredField.getName());
                declaredField.setAccessible(true);
                iterate(declaredField.getType(), declaredField.get(parent), parentString + declaredField.getName() + ".", recursive);
            }
        }
    }

    static Field getField(String feature) {
        return STRING_TO_FIELD.get(ConfigHelper.redirect(feature));
    }

    static String getString(Field field) {
        return FIELD_TO_STRING.get(field.getName());
    }

    @SneakyThrows(IllegalAccessException.class)
    public static <T> T get(String feature) throws NoSuchFieldException {
        feature = check(feature);
        return Utilities.cast(getterFunc.get(feature));
    }

    @SneakyThrows(IllegalAccessException.class)
    public static Field set(String feature, Object value) throws NoSuchFieldException {
        feature = check(feature);
        setterFunc.set(feature, value);
        return getField(feature);
    }

    private static String check(String feature) throws NoSuchFieldException {
        feature = ConfigHelper.redirect(feature);
        if (!FILTERED.containsKey(feature)) {
            throw new NoSuchFieldException("Feature " + feature + " does not exist or is blocked");
        }
        return feature;
    }

    public static Set<String> getFeatures() {
        return FILTERED.keySet();
    }

    public static AndromedaConfig get() {
        return CONFIG;
    }

    public static AndromedaConfig getDefault() {
        return Default.DEFAULT;
    }

    private static class Default {

        static final AndromedaConfig DEFAULT = new AndromedaConfig();
    }

    private interface Getter {
        Object get(String feature) throws NoSuchFieldException, IllegalAccessException;
    }

    private interface Setter {
        void set(String feature, Object value) throws NoSuchFieldException, IllegalAccessException;
    }
}

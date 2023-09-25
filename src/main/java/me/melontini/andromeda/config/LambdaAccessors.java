package me.melontini.andromeda.config;

import me.melontini.dark_matter.api.base.reflect.MiscReflection;
import me.melontini.dark_matter.api.config.ConfigBuilder;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class LambdaAccessors {

    private static final Map<String, List<Function<Object, Object>>> GETTERS = new HashMap<>();
    private static final Map<String, BiConsumer<Object, Object>> SETTERS = new HashMap<>();

    static ConfigBuilder<AndromedaConfig> attach(ConfigBuilder<AndromedaConfig> builder) {
        MethodHandles.Lookup  lookup = MethodHandles.lookup();
        return builder.scanner((cls, currentField, parentString, recursive, fieldRefView) -> {
            String name = parentString + currentField.getName();
            for (Field field : fieldRefView) {
                List<Function<Object, Object>> getters = GETTERS.computeIfAbsent(name, s -> new ArrayList<>());

                try {
                    getters.add(MiscReflection.createGetter(field, lookup));
                } catch (Exception e) {
                    getters.add(o -> {
                        try {
                            return field.get(o);
                        } catch (IllegalAccessException e1) {
                            throw new RuntimeException(e1);
                        }
                    });
                }

                try {
                    SETTERS.put(name, MiscReflection.createSetter(field, lookup));
                } catch (Exception e) {
                    SETTERS.put(name, (o, v) -> {
                        try {
                            field.set(o, v);
                        } catch (IllegalAccessException e1) {
                            throw new RuntimeException(e1);
                        }
                    });
                }
            }
        }).getter((configManager, option) -> {
            List<Function<Object, Object>> getters = GETTERS.get(option);
            if (getters == null) throw new NoSuchFieldException(option);

            Object obj = configManager.getConfig();
            for (Function<Object, Object> getter : getters) {
                obj = getter.apply(obj);
            }
            return obj;
        }).setter((manager, option, value) -> {
            List<Function<Object, Object>> getters = GETTERS.get(option);
            if (getters == null) throw new NoSuchFieldException(option);

            Object obj = manager.getConfig();
            for (int i = 0; i < getters.size() - 1; i++) {
                obj = getters.get(i).apply(obj);
            }
            SETTERS.get(option).accept(obj, value);
        });
    }
}

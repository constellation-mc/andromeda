package me.melontini.andromeda.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.melontini.andromeda.util.AndromedaLog;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

class Fixup {

    private static final Map<String, Set<BiFunction<JsonObject, JsonElement, Boolean>>> FIXUPS = new HashMap<>();

    static JsonObject fixup(JsonObject object) {
        for (Map.Entry<String, Set<BiFunction<JsonObject, JsonElement, Boolean>>> entry : FIXUPS.entrySet()) {
            if (object.has(entry.getKey())) {
                entry.getValue().forEach(function -> {
                    if (function.apply(object, object.get(entry.getKey())))
                        AndromedaLog.info("Fixed-up config entry: " + entry.getKey());
                });
            }
        }
        return object;
    }

    static void addFixup(final String key, final BiFunction<JsonObject, JsonElement, Boolean> fixup) {
        FIXUPS.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(fixup);
    }

    static {
        addFixup("throwableItems", (object, element) -> {
            if (element instanceof JsonPrimitive primitive && primitive.isBoolean()) {
                boolean value = primitive.getAsBoolean();
                object.remove("throwableItems");

                JsonObject throwableItems = new JsonObject();
                throwableItems.addProperty("enable", value);
                object.add("throwableItems", throwableItems);
                return true;
            }
            return false;
        });
        addFixup("throwableItemsBlacklist", (object, element) -> {
            if (element instanceof JsonArray array) {
                object.remove("throwableItemsBlacklist");

                JsonObject throwableItems = object.get("throwableItems").getAsJsonObject();
                throwableItems.add("blacklist", array);
                return true;
            }
            return false;
        });
    }
}

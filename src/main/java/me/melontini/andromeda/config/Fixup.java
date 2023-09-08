package me.melontini.andromeda.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.base.util.MakeSure;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

class Fixup {

    private static final Map<String, Set<BiFunction<JsonObject, JsonElement, Boolean>>> FIXUPS = new HashMap<>();

    static JsonObject fixup(JsonObject object) {
        MakeSure.notNull(object);

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

        addFixup("incubatorSettings", (object, element) -> {
            if (element instanceof JsonObject o) {
                object.remove("incubatorSettings");

                surgery(o, o, "enableIncubator", "enable");
                surgery(o, o, "incubatorRandomness", "randomness");
                surgery(o, o, "incubatorRecipe", "recipe");

                object.add("incubator", o);
                return true;
            }
            return false;
        });

        addFixup("autogenRecipeAdvancements", (object, element) -> {
            if (element instanceof JsonObject o) {
                object.remove("autogenRecipeAdvancements");

                surgery(o, o, "autogenRecipeAdvancements", "enable");
                surgery(o, o, "blacklistedRecipeNamespaces", "namespaceBlacklist");
                surgery(o, o, "blacklistedRecipeIds", "recipeBlacklist");

                object.add("recipeAdvancementsGeneration", o);
                return true;
            }
            return false;
        });

        addFixup("campfireTweaks", (object, element) -> {
            if (element instanceof JsonObject o) {

                surgery(o, o, "campfireEffects", "effects");
                surgery(o, o, "campfireEffectsPassive", "affectsPassive");
                surgery(o, o, "campfireEffectsRange", "effectsRange");

                return true;
            }
            return false;
        });
    }

    private static void surgery(JsonObject donor, JsonObject patient, String oldKey, String newKey) {
        if (donor.has(oldKey)) {
            patient.add(newKey, donor.get(oldKey));
            donor.remove(oldKey);
        }
    }
}

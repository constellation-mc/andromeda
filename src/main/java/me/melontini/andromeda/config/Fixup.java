package me.melontini.andromeda.config;

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
                object.remove("throwableItems");

                JsonObject throwableItems = new JsonObject();
                throwableItems.addProperty("enable", primitive.getAsBoolean());
                surgery(object, throwableItems, "throwableItemsBlacklist", "blacklist");
                object.add("throwableItems", throwableItems);
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
                boolean mod = false;

                mod |= surgery(o, o, "campfireEffects", "effects");
                mod |= surgery(o, o, "campfireEffectsPassive", "affectsPassive");
                mod |= surgery(o, o, "campfireEffectsRange", "effectsRange");

                return mod;
            }
            return false;
        });
    }

    private static boolean surgery(JsonObject donor, JsonObject patient, String oldKey, String newKey) {
        if (donor.has(oldKey)) {
            patient.add(newKey, donor.get(oldKey));
            donor.remove(oldKey);
            return true;
        }
        return false;
    }
}

package me.melontini.andromeda.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.classes.TriFunction;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

class Fixup {

    private static final Map<String, Set<TriFunction<JsonObject, JsonElement, String, Boolean>>> FIXUPS = new LinkedHashMap<>();

    static JsonObject fixup(JsonObject object) {
        MakeSure.notNull(object);

        for (Map.Entry<String, Set<TriFunction<JsonObject, JsonElement, String, Boolean>>> entry : FIXUPS.entrySet()) {
            if (object.has(entry.getKey())) {
                entry.getValue().forEach(function -> {
                    if (function.apply(object, object.get(entry.getKey()), entry.getKey()))
                        AndromedaLog.info("Fixed-up config entry: " + entry.getKey());
                });
            }
        }
        return object;
    }

    static void addFixup(final String key, final TriFunction<JsonObject, JsonElement, String, Boolean> fixup) {
        FIXUPS.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(fixup);
    }

    static {
        addFixup("throwableItems", (object, element, key) -> {
            if (element instanceof JsonPrimitive p && p.isBoolean()) {
                object.remove(key);

                JsonObject throwableItems = new JsonObject();
                throwableItems.addProperty("enable", p.getAsBoolean());
                surgery(object, throwableItems, "throwableItemsBlacklist", "blacklist");
                object.add(key, throwableItems);
                return true;
            }
            return false;
        });

        addFixup("incubatorSettings", (object, element, key) -> {
            if (element instanceof JsonObject o) {
                object.remove(key);

                surgery(o, o, "enableIncubator", "enable");
                surgery(o, o, "incubatorRandomness", "randomness");
                surgery(o, o, "incubatorRecipe", "recipe");

                object.add("incubator", o);
                return true;
            }
            return false;
        });

        addFixup("autogenRecipeAdvancements", (object, element, key) -> {
            if (element instanceof JsonObject o) {
                object.remove(key);

                surgery(o, o, "autogenRecipeAdvancements", "enable");
                surgery(o, o, "blacklistedRecipeNamespaces", "namespaceBlacklist");
                surgery(o, o, "blacklistedRecipeIds", "recipeBlacklist");

                object.add("recipeAdvancementsGeneration", o);
                return true;
            }
            return false;
        });

        addFixup("campfireTweaks", (object, element, key) -> {
            if (element instanceof JsonObject o) {
                boolean mod = false;

                mod |= surgery(o, o, "campfireEffects", "effects");
                mod |= surgery(o, o, "campfireEffectsPassive", "affectsPassive");
                mod |= surgery(o, o, "campfireEffectsRange", "effectsRange");

                return mod;
            }
            return false;
        });

        addFixup("campfireTweaks", (object, element, key) -> {
            if (element instanceof JsonObject o) {
                if (o.has("campfireEffectsList") && o.has("campfireEffectsAmplifierList")) {
                    if (o.has("effectList")) o.remove("effectList"); //This shouldn't happen tbh.

                    JsonArray effectList = new JsonArray();
                    JsonArray oldEffectList = o.getAsJsonArray("campfireEffectsList");
                    JsonArray oldAmplifierList = o.getAsJsonArray("campfireEffectsAmplifierList");

                    for (int i = 0; i < oldEffectList.size(); i++) {
                        JsonObject effect = new JsonObject();
                        effect.addProperty("identifier", oldEffectList.get(i).getAsString());
                        effect.addProperty("amplifier", oldAmplifierList.get(i).getAsInt());
                        effectList.add(effect);
                    }

                    o.add("effectList", effectList);
                    o.remove("campfireEffectsList");
                    o.remove("campfireEffectsAmplifierList");
                    return true;
                }
                return false;
            }
            return false;
        });

        addFixup("selfPlanting", (object, element, s) -> {
            if (element instanceof JsonPrimitive p && p.isBoolean()) {
                JsonObject autoPlanting = object.has("autoPlanting") ? object.getAsJsonObject("autoPlanting") : new JsonObject();
                autoPlanting.addProperty("enabled", p.getAsBoolean());
                if (!object.has("autoPlanting")) object.add("autoPlanting", autoPlanting);
                return true;
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

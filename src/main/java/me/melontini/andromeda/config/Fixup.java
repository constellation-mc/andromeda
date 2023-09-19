package me.melontini.andromeda.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.melontini.dark_matter.api.config.FixupsBuilder;

@SuppressWarnings("UnstableApiUsage")
class Fixup {

    static void addFixups(FixupsBuilder builder) {
        builder.add("throwableItems", (object, element, key) -> {
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

        builder.add("incubatorSettings", (object, element, key) -> {
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

        builder.add("autogenRecipeAdvancements", (object, element, key) -> {
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

        builder.add("campfireTweaks", (object, element, key) -> {
            if (element instanceof JsonObject o) {
                boolean mod = false;

                mod |= surgery(o, o, "campfireEffects", "effects");
                mod |= surgery(o, o, "campfireEffectsPassive", "affectsPassive");
                mod |= surgery(o, o, "campfireEffectsRange", "effectsRange");

                return mod;
            }
            return false;
        });

        builder.add("campfireTweaks", (object, element, key) -> {
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

        builder.add("selfPlanting", (object, element, s) -> {
            if (element instanceof JsonPrimitive p && p.isBoolean()) {
                JsonObject autoPlanting = object.has("autoPlanting") ? object.getAsJsonObject("autoPlanting") : new JsonObject();
                autoPlanting.addProperty("enabled", p.getAsBoolean());
                if (!object.has("autoPlanting")) object.add("autoPlanting", autoPlanting);
                return true;
            }
            return false;
        });

        builder.add("bedExplosionPower", (object, element, s) -> {
            if (!object.has("enableBedExplosionPower") && element instanceof JsonPrimitive p) {
                if (p.getAsFloat() != 5.0F) {
                    object.addProperty("enableBedExplosionPower", true);
                    return true;
                }
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

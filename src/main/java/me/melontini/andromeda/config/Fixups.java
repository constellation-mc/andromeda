package me.melontini.andromeda.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.melontini.dark_matter.api.config.serializers.gson.FixupsBuilder;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
class Fixups {

    static me.melontini.dark_matter.api.config.serializers.gson.Fixups addFixups() {
        FixupsBuilder builder = FixupsBuilder.create();
        builder.add("throwableItems", (holder) -> {
            if (holder.value() instanceof JsonPrimitive p && p.isBoolean()) {
                holder.config().remove("throwableItems");

                JsonObject throwableItems = new JsonObject();
                throwableItems.addProperty("enable", p.getAsBoolean());
                surgery(holder.config(), throwableItems, "throwableItemsBlacklist", "blacklist");
                holder.config().add("throwableItems", throwableItems);
                return true;
            }
            return false;
        });

        builder.add("incubatorSettings", (holder) -> {
            if (holder.value() instanceof JsonObject o) {
                holder.config().remove("incubatorSettings");

                surgery(o, o, "enableIncubator", "enable");
                surgery(o, o, "incubatorRandomness", "randomness");
                surgery(o, o, "incubatorRecipe", "recipe");

                holder.config().add("incubator", o);
                return true;
            }
            return false;
        });

        builder.add("autogenRecipeAdvancements", (holder) -> {
            if (holder.value() instanceof JsonObject o) {
                holder.config().remove("autogenRecipeAdvancements");

                surgery(o, o, "autogenRecipeAdvancements", "enable");
                surgery(o, o, "blacklistedRecipeNamespaces", "namespaceBlacklist");
                surgery(o, o, "blacklistedRecipeIds", "recipeBlacklist");

                holder.config().add("recipeAdvancementsGeneration", o);
                return true;
            }
            return false;
        });

        Map<String, String> ctMove = Map.of("campfireTweaks.campfireEffects", "effects", "campfireTweaks.campfireEffectsPassive", "affectsPassive", "campfireTweaks.campfireEffectsRange", "effectsRange");
        ctMove.forEach((s, s2) -> builder.add(s, (holder) ->
                holder.parent() != null && surgery(holder.parent(), holder.parent(), holder.key()[holder.key().length - 1], s2)));

        builder.add("campfireTweaks", (holder) -> {
            if (holder.value() instanceof JsonObject o) {
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

        builder.add("selfPlanting", (holder) -> {
            if (holder.value() instanceof JsonPrimitive p && p.isBoolean()) {
                JsonObject autoPlanting = holder.config().has("autoPlanting") ? holder.config().getAsJsonObject("autoPlanting") : new JsonObject();
                autoPlanting.addProperty("enabled", p.getAsBoolean());
                if (!holder.config().has("autoPlanting")) holder.config().add("autoPlanting", autoPlanting);
                return true;
            }
            return false;
        });

        builder.add("bedExplosionPower", (holder) -> {
            if (!holder.config().has("enableBedExplosionPower") && holder.value() instanceof JsonPrimitive p) {
                if (p.getAsFloat() != 5.0F) {
                    holder.config().addProperty("enableBedExplosionPower", true);
                    return true;
                }
            }
            return false;
        });

        return builder.build();
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

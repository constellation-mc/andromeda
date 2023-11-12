package me.melontini.andromeda.registries;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import static me.melontini.andromeda.registries.Common.id;

public class ResourceRegistry {

    public static void init() {
        ResourceConditions.register(id("config_option"), object -> {
            JsonArray array = JsonHelper.getArray(object, "values");
            boolean load = true;

            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    String configOption = element.getAsString();
                    try {
                        load = Config.get(configOption);
                    } catch (NoSuchFieldException e) {
                        throw new AndromedaException("Invalid config option: " + configOption);
                    }
                    if (!load) break;
                }
            }

            return load;
        });
        ResourceConditions.register(id("items_registered"), object -> {
            JsonArray array = JsonHelper.getArray(object, "values");

            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    if (!Registry.ITEM.containsId(Identifier.tryParse(element.getAsString()))) return false;
                }
            }

            return true;
        });
    }

    public static <T> T parseFromId(String id, Registry<T> registry) {
        Identifier identifier = Identifier.tryParse(id);
        if (!registry.containsId(identifier)) throw new InvalidIdentifierException(String.format("(Andromeda) invalid identifier provided! id: %s, registry: %s", identifier, registry));
        return registry.get(identifier);
    }
}

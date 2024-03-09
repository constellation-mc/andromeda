package me.melontini.andromeda.common.registries;

import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class Common {

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static void bootstrap() {
        AndromedaItemGroup.init();

        ResourceConditions.register(id("items_registered"), object -> Streams.stream(JsonHelper.getArray(object, "values").iterator())
                .filter(JsonElement::isJsonPrimitive)
                .allMatch(e -> CommonRegistries.items().containsId(Identifier.tryParse(e.getAsString()))));
    }
}

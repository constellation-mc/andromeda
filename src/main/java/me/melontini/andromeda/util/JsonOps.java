package me.melontini.andromeda.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class JsonOps {

    public static void ifPresent(JsonObject o, String key, Consumer<JsonElement> e) {
        if (o.has(key)) {
            e.accept(o.get(key));
        }
    }
}

package me.melontini.andromeda.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class JsonDataLoader implements SimpleResourceReloadListener<Map<Identifier, JsonObject>> {

    private final Gson gson;
    private final String dataType;

    public JsonDataLoader(Gson gson, String dataType) {
        this.gson = gson;
        this.dataType = dataType;
    }

    @Override
    public CompletableFuture<Map<Identifier, JsonObject>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, JsonObject> map = Maps.newHashMap();
            int i = this.dataType.length() + 1;

            manager.findResources(this.dataType, (id) -> id.endsWith(".json")).forEach((id) -> {
                String string = id.getPath();
                Identifier id2 = new Identifier(id.getNamespace(), string.substring(i, string.length() - ".json".length()));

                try (Resource resource = manager.getResource(id); InputStream stream = resource.getInputStream(); Reader reader = new InputStreamReader(stream)) {
                    JsonObject object = JsonHelper.deserialize(this.gson, reader, JsonObject.class);
                    if (object != null) {
                        if (object.has(ResourceConditions.CONDITIONS_KEY))
                            if (!ResourceConditions.objectMatchesConditions(object)) return;

                        JsonObject jsonElement2 = map.put(id2, object);
                        if (jsonElement2 != null) {
                            throw new IllegalStateException("Duplicate data file ignored with ID " + id2);
                        }
                    } else {
                        AndromedaLog.error("Couldn't load data file {} from {} as it's null or empty", id2, id);
                    }
                } catch (IllegalArgumentException | IOException | JsonParseException e) {
                    AndromedaLog.error("Couldn't parse data file {} from {}", id2, id, e);
                }
            });

            return map;
        }, executor);
    }
}

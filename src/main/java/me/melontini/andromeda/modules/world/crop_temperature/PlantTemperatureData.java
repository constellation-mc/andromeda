package me.melontini.andromeda.modules.world.crop_temperature;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.melontini.andromeda.util.JsonDataLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static me.melontini.andromeda.registries.ResourceRegistry.parseFromId;
import static me.melontini.andromeda.util.CommonValues.MODID;

public record PlantTemperatureData(Block block, float min, float max, float aMin, float aMax) {

    public static final Map<Block, PlantTemperatureData> PLANT_DATA = new HashMap<>();

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PlantTemperatureData.PLANT_DATA.clear());

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new JsonDataLoader(new Gson(), "andromeda/crop_temperatures") {
            @Override
            public CompletableFuture<Void> apply(Map<Identifier, JsonObject> data, ResourceManager manager, Profiler profiler, Executor executor) {
                return CompletableFuture.supplyAsync(() -> {
                    Map<Identifier, PlantTemperatureData> map = new HashMap<>();

                    data.forEach((identifier, object) -> map.put(identifier, new PlantTemperatureData(parseFromId(object.get("identifier").getAsString(), Registries.BLOCK),
                            object.get("min").getAsFloat(),
                            object.get("max").getAsFloat(),
                            object.get("aMin").getAsFloat(),
                            object.get("aMax").getAsFloat())));

                    return map;
                }, executor).thenAcceptAsync(map -> map.forEach((identifier, temperatureData) ->
                        PLANT_DATA.put(temperatureData.block, temperatureData)), executor);
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier(MODID, "crop_temperatures");
            }
        });
    }
}

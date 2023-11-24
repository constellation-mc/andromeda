package me.melontini.andromeda.modules.world.crop_temperature;

import com.google.gson.JsonObject;
import me.melontini.andromeda.util.AndromedaLog;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.block.Block;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import static me.melontini.andromeda.registries.ResourceRegistry.parseFromId;
import static me.melontini.andromeda.util.CommonValues.MODID;

public record PlantTemperatureData(Block block, float min, float max, float aMin, float aMax) {

    public static final Map<Block, PlantTemperatureData> PLANT_DATA = new HashMap<>();

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PlantTemperatureData.PLANT_DATA.clear());

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(MODID, "crop_temperatures");
            }

            @Override
            public void reload(ResourceManager manager) {
                PLANT_DATA.clear();
                var map = manager.findResources("am_crop_temperatures", identifier -> identifier.getPath().endsWith(".json"));
                for (Map.Entry<Identifier, Resource> entry : map.entrySet()) {
                    try (InputStream stream = entry.getValue().getInputStream(); Reader reader = new InputStreamReader(stream)) {
                        JsonObject object = JsonHelper.deserialize(reader);
                        if (!ResourceConditions.objectMatchesConditions(object)) continue;

                        Block block = parseFromId(object.get("identifier").getAsString(), Registry.BLOCK);
                        PLANT_DATA.putIfAbsent(block, new PlantTemperatureData(
                                block,
                                object.get("min").getAsFloat(),
                                object.get("max").getAsFloat(),
                                object.get("aMin").getAsFloat(),
                                object.get("aMax").getAsFloat()
                        ));
                    } catch (Exception e) {
                        AndromedaLog.error("Error while loading am_crop_temperatures. id: " + entry.getKey(), e);
                    }
                }
            }
        });
    }
}

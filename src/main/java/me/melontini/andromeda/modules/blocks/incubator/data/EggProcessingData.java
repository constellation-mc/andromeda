package me.melontini.andromeda.modules.blocks.incubator.data;

import com.google.gson.JsonObject;
import me.melontini.andromeda.util.AndromedaLog;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
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

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.registries.ResourceRegistry.parseFromId;

public record EggProcessingData(Item item, EntityType<?> entity, int time) {

    public static Map<Item, EggProcessingData> EGG_DATA = new HashMap<>();

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            EggProcessingData.EGG_DATA.clear();
        });

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return id("egg_processing");
            }

            @Override
            public void reload(ResourceManager manager) {
                EggProcessingData.EGG_DATA.clear();
                //well...
                for (Item item : Registry.ITEM) {
                    if (item instanceof SpawnEggItem spawnEggItem) {
                        EggProcessingData.EGG_DATA.putIfAbsent(spawnEggItem, new EggProcessingData(spawnEggItem, spawnEggItem.getEntityType(new NbtCompound()), 8000));
                    }
                }

                var map = manager.findResources("am_egg_processing", identifier -> identifier.getPath().endsWith(".json"));
                for (Map.Entry<Identifier, Resource> entry : map.entrySet()) {
                    try (InputStream stream = entry.getValue().getInputStream(); Reader reader = new InputStreamReader(stream)) {
                        JsonObject object = JsonHelper.deserialize(reader);
                        if (!ResourceConditions.objectMatchesConditions(object)) continue;

                        EntityType<?> entity = parseFromId(object.get("entity").getAsString(), Registry.ENTITY_TYPE);
                        Item item = parseFromId(object.get("identifier").getAsString(), Registry.ITEM);
                        EggProcessingData.EGG_DATA.putIfAbsent(item, new EggProcessingData(item, entity, object.get("time").getAsInt()));
                    } catch (Exception e) {
                        AndromedaLog.error("Error while loading am_egg_processing. id: " + entry.getKey(), e);
                    }
                }
            }
        });
    }
}

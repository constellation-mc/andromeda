package me.melontini.andromeda.modules.blocks.incubator.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.melontini.andromeda.util.JsonDataLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.registries.ResourceRegistry.parseFromId;

public record EggProcessingData(Item item, EntityType<?> entity, int time) {

    public static Map<Item, EggProcessingData> EGG_DATA = new HashMap<>();

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> EggProcessingData.EGG_DATA.clear());

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new JsonDataLoader(new Gson(), "andromeda/egg_processing") {

            @Override
            public Identifier getFabricId() {
                return id("egg_processing");
            }

            @Override
            public CompletableFuture<Void> apply(Map<Identifier, JsonObject> data, ResourceManager manager, Profiler profiler, Executor executor) {
                return CompletableFuture.supplyAsync(() -> {
                    Map<Identifier, EggProcessingData> map = new HashMap<>();

                    data.forEach((identifier, object) -> {
                        EntityType<?> entity = parseFromId(object.get("entity").getAsString(), Registries.ENTITY_TYPE);
                        Item item = parseFromId(object.get("identifier").getAsString(), Registries.ITEM);

                        map.put(identifier, new EggProcessingData(item, entity, object.get("time").getAsInt()));
                    });

                    return map;
                }, executor).thenAcceptAsync(map -> {
                    EggProcessingData.EGG_DATA.clear();
                    //well...
                    for (Item item : Registries.ITEM) {
                        if (item instanceof SpawnEggItem spawnEggItem) {
                            EggProcessingData.EGG_DATA.putIfAbsent(spawnEggItem, new EggProcessingData(spawnEggItem, spawnEggItem.getEntityType(new NbtCompound()), 8000));
                        }
                    }

                    map.forEach((identifier, data1) -> EggProcessingData.EGG_DATA.put(data1.item(), data1));
                }, executor);
            }
        });
    }
}

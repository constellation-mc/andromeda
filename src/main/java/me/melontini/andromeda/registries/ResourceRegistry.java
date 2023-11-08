package me.melontini.andromeda.registries;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorAdder;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.data.EggProcessingData;
import me.melontini.andromeda.util.data.ItemBehaviorData;
import me.melontini.andromeda.util.data.PlantTemperatureData;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

import static me.melontini.andromeda.registries.Common.id;

public class ResourceRegistry {

    static void register() {
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

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return id("crop_temperatures");
            }

            @Override
            public void reload(ResourceManager manager) {
                Andromeda.get().PLANT_DATA.clear();
                if (!Config.get().temperatureBasedCropGrowthSpeed) return;

                var map = manager.findResources("am_crop_temperatures", identifier -> identifier.getPath().endsWith(".json"));
                for (Map.Entry<Identifier, Resource> entry : map.entrySet()) {
                    try (InputStream stream = entry.getValue().getInputStream(); Reader reader = new InputStreamReader(stream)) {
                        JsonObject object = JsonHelper.deserialize(reader);
                        if (!ResourceConditions.objectMatchesConditions(object)) continue;

                        Block block = parseFromId(object.get("identifier").getAsString(), Registry.BLOCK);
                        Andromeda.get().PLANT_DATA.putIfAbsent(block, new PlantTemperatureData(
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


        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return id("egg_processing");
            }

            @Override
            public void reload(ResourceManager manager) {
                Andromeda.get().EGG_DATA.clear();
                if (!Config.get().incubator.enable) return;

                //well...
                for (Item item : Registry.ITEM) {
                    if (item instanceof SpawnEggItem spawnEggItem) {
                        Andromeda.get().EGG_DATA.putIfAbsent(spawnEggItem, new EggProcessingData(spawnEggItem, spawnEggItem.getEntityType(new NbtCompound()), 8000));
                    }
                }

                var map = manager.findResources("am_egg_processing", identifier -> identifier.getPath().endsWith(".json"));
                for (Map.Entry<Identifier, Resource> entry : map.entrySet()) {
                    try (InputStream stream = entry.getValue().getInputStream(); Reader reader = new InputStreamReader(stream)) {
                        JsonObject object = JsonHelper.deserialize(reader);
                        if (!ResourceConditions.objectMatchesConditions(object)) continue;

                        EntityType<?> entity = parseFromId(object.get("entity").getAsString(), Registry.ENTITY_TYPE);
                        Item item = parseFromId(object.get("identifier").getAsString(), Registry.ITEM);
                        Andromeda.get().EGG_DATA.putIfAbsent(item, new EggProcessingData(item, entity, object.get("time").getAsInt()));
                    } catch (Exception e) {
                        AndromedaLog.error("Error while loading am_egg_processing. id: " + entry.getKey(), e);
                    }
                }
            }
        });

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return id("am_item_throw_behavior");
            }

            @Override
            public void reload(ResourceManager manager) {
                ItemBehaviorManager.clear();
                if (!Config.get().throwableItems.enable) return;

                EntrypointRunner.runEntrypoint("andromeda:collect_behaviors", Runnable.class, Runnable::run);

                var map = manager.findResources("am_item_throw_behavior", identifier -> identifier.getPath().endsWith(".json"));
                for (Map.Entry<Identifier, Resource> entry : map.entrySet()) {
                    try (InputStream stream = entry.getValue().getInputStream(); Reader reader = new InputStreamReader(stream)) {
                        JsonObject object = JsonHelper.deserialize(reader);
                        if (!ResourceConditions.objectMatchesConditions(object)) continue;

                        ItemBehaviorData data = new ItemBehaviorData();

                        Set<Item> items = new HashSet<>();

                        if (!object.has("item_id")) throw new InvalidIdentifierException("(Andromeda) missing item_id!");

                        JsonElement element = object.get("item_id");
                        if (element.isJsonArray()) {
                            element.getAsJsonArray().forEach(e -> items.add(parseFromId(e.getAsString(), Registry.ITEM)));
                        } else {
                            items.add(parseFromId(element.getAsString(), Registry.ITEM));
                        }

                        data.on_entity_hit = readCommands(JsonHelper.getObject(object, "on_entity_hit",  new JsonObject()));
                        data.on_block_hit = readCommands(JsonHelper.getObject(object, "on_block_hit",  new JsonObject()));
                        data.on_miss = readCommands(JsonHelper.getObject(object, "on_miss",  new JsonObject()));
                        data.on_any_hit = readCommands(JsonHelper.getObject(object, "on_any_hit",  new JsonObject()));

                        data.spawn_item_particles = JsonHelper.getBoolean(object, "spawn_item_particles", true);
                        data.spawn_colored_particles = JsonHelper.getBoolean(object, "spawn_colored_particles", false);
                        JsonObject colors = JsonHelper.getObject(object, "particle_colors", new JsonObject());
                        data.particle_colors = new ItemBehaviorData.ParticleColors(
                                JsonHelper.getInt(colors, "red", 0),
                                JsonHelper.getInt(colors, "green", 0),
                                JsonHelper.getInt(colors, "blue", 0)
                        );

                        boolean override_vanilla = JsonHelper.getBoolean(object, "override_vanilla",  false);
                        boolean complement = JsonHelper.getBoolean(object, "complement",  true);
                        int cooldown_time = JsonHelper.getInt(object, "cooldown", 50);

                        for (Item item : items) {
                            ItemBehaviorManager.addBehavior(item, ItemBehaviorAdder.dataPack(data), complement);
                            if (override_vanilla) ItemBehaviorManager.overrideVanilla(item);

                            if (cooldown_time != 50) {
                                ItemBehaviorManager.addCustomCooldown(item, cooldown_time);
                            }
                        }
                    } catch (Exception e) {
                        AndromedaLog.error("Error while loading am_item_throw_behavior. id: " + entry.getKey(), e);
                    }
                }
            }
        });

        AndromedaLog.info("ResourceRegistry init complete!");
    }

    private static <T> T parseFromId(String id, Registry<T> registry) {
        Identifier identifier = Identifier.tryParse(id);
        if (!registry.containsId(identifier)) throw new InvalidIdentifierException(String.format("(Andromeda) invalid identifier provided! id: %s, registry: %s", identifier, registry));
        return registry.get(identifier);
    }

    private static ItemBehaviorData.CommandHolder readCommands(JsonObject json) {
        return new ItemBehaviorData.CommandHolder(readCommands(json, "item_commands"),
                readCommands(json, "user_commands"),
                readCommands(json, "server_commands"),
                readCommands(json, "hit_entity_commands"),
                readCommands(json, "hit_block_commands"));
    }

    private static List<String> readCommands(JsonObject json, String source) {
        var item_arr = JsonHelper.getArray(json, source, null);
        if (item_arr != null) {
            List<String> commands = new ArrayList<>();
            for (JsonElement element : item_arr) {
                commands.add(element.getAsString());
            }
            return commands;
        }
        return null;
    }
}

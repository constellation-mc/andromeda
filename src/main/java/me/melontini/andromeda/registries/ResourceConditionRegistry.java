package me.melontini.andromeda.registries;

import com.google.gson.*;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorAdder;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.ConfigHelper;
import me.melontini.andromeda.util.data.EggProcessingData;
import me.melontini.andromeda.util.data.ItemBehaviorData;
import me.melontini.andromeda.util.data.PlantTemperatureData;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static me.melontini.andromeda.util.SharedConstants.MODID;

public class ResourceConditionRegistry {

    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void register() {
        ResourceConditions.register(new Identifier(MODID, "config_option"), object -> {
            JsonArray array = JsonHelper.getArray(object, "values");
            boolean load = true;

            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    try {
                        String configOption = element.getAsString();
                        try {
                            load = (boolean) ConfigHelper.getConfigOption(configOption, Andromeda.CONFIG);
                        } catch (NoSuchFieldException e) {
                            throw new AndromedaException("Invalid config option: " + configOption);
                        }
                        if (!load) break;
                    } catch (IllegalAccessException e) {
                        throw new AndromedaException("Exception while evaluating andromeda:config_option", e);
                    }
                }
            }

            return load;
        });
        ResourceConditions.register(new Identifier(MODID, "items_registered"), object -> {
            JsonArray array = JsonHelper.getArray(object, "values");

            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    if (Registry.ITEM.get(new Identifier(element.getAsString())) == Items.AIR) return false;
                }
            }

            return true;
        });

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(MODID, "crop_temperatures");
            }

            @Override
            public void reload(ResourceManager manager) {
                Andromeda.PLANT_DATA.clear();
                var map = manager.findResources("am_crop_temperatures", identifier -> identifier.getPath().endsWith(".json"));
                for (Map.Entry<Identifier, Resource> entry : map.entrySet()) {
                    try {
                        JsonObject object = JsonHelper.deserialize(new InputStreamReader(entry.getValue().getInputStream()));
                        //LogUtil.devInfo(object);
                        PlantTemperatureData data = new PlantTemperatureData(
                                Identifier.tryParse(object.get("identifier").getAsString()),
                                object.get("min").getAsFloat(),
                                object.get("max").getAsFloat(),
                                object.get("aMin").getAsFloat(),
                                object.get("aMax").getAsFloat()
                        );

                        if (Registry.BLOCK.get(data.identifier()) == Blocks.AIR) {
                            throw new InvalidIdentifierException(String.format("(Andromeda) invalid identifier provided! %s", data.identifier()));
                        }

                        Andromeda.PLANT_DATA.putIfAbsent(Registry.BLOCK.get(data.identifier()), data);
                    } catch (IOException e) {
                        AndromedaLog.error("Error while parsing JSON for am_crop_temperatures", e);
                    }
                }
            }
        });


        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(MODID, "egg_processing");
            }

            @Override
            public void reload(ResourceManager manager) {
                Andromeda.EGG_DATA.clear();
                //well...
                for (Item item : Registry.ITEM) {
                    if (item instanceof SpawnEggItem spawnEggItem) {
                        Andromeda.EGG_DATA.putIfAbsent(spawnEggItem, new EggProcessingData(Registry.ITEM.getId(spawnEggItem).toString(), Registry.ENTITY_TYPE.getId(spawnEggItem.getEntityType(new NbtCompound())).toString(), 8000));
                    }
                }

                var map = manager.findResources("am_egg_processing", identifier -> identifier.getPath().endsWith(".json"));
                for (Map.Entry<Identifier, Resource> entry : map.entrySet()) {
                    try {
                        var jsonElement = JsonHelper.deserialize(new InputStreamReader(entry.getValue().getInputStream()));
                        //LogUtil.devInfo(jsonElement);
                        EggProcessingData data = GSON.fromJson(jsonElement, EggProcessingData.class);

                        if (Registry.ENTITY_TYPE.get(Identifier.tryParse(data.entity)) == EntityType.PIG && !Objects.equals(data.entity, "minecraft:pig")) {
                            throw new InvalidIdentifierException(String.format("(Andromeda) invalid entity identifier provided! %s", data.entity));
                        }

                        if (Registry.ITEM.get(Identifier.tryParse(data.identifier)) == Items.AIR) {
                            throw new InvalidIdentifierException(String.format("(Andromeda) invalid item identifier provided! %s", data.identifier));
                        }

                        Andromeda.EGG_DATA.putIfAbsent(Registry.ITEM.get(Identifier.tryParse(data.identifier)), data);
                    } catch (IOException e) {
                        AndromedaLog.error("Error while parsing JSON for am_egg_processing", e);
                    }
                }
            }
        });

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(MODID, "am_item_throw_behavior");
            }

            @Override
            public void reload(ResourceManager manager) {
                ItemBehaviorManager.clear();
                FabricLoader.getInstance().getEntrypoints("andromeda:collect_behaviors", Runnable.class).forEach(Runnable::run);

                var map = manager.findResources("am_item_throw_behavior", identifier -> identifier.getPath().endsWith(".json"));
                for (Map.Entry<Identifier, Resource> entry : map.entrySet()) {
                    try {
                        JsonObject json = JsonHelper.deserialize(new InputStreamReader(entry.getValue().getInputStream()));
                        ItemBehaviorData data = new ItemBehaviorData();

                        Set<Item> items = new HashSet<>();

                        if (!json.has("item_id")) throw new InvalidIdentifierException("(Andromeda) missing item_id!");

                        JsonElement element = json.get("item_id");
                        if (element.isJsonArray()) {
                            element.getAsJsonArray().forEach(e -> items.add(parseItemFromId(e.getAsString())));
                        } else {
                            items.add(parseItemFromId(element.getAsString()));
                        }

                        data.on_entity_hit = readCommands(JsonHelper.getObject(json, "on_entity_hit",  new JsonObject()));
                        data.on_block_hit = readCommands(JsonHelper.getObject(json, "on_block_hit",  new JsonObject()));
                        data.on_miss = readCommands(JsonHelper.getObject(json, "on_miss",  new JsonObject()));
                        data.on_any_hit = readCommands(JsonHelper.getObject(json, "on_any_hit",  new JsonObject()));

                        data.spawn_item_particles = JsonHelper.getBoolean(json, "spawn_item_particles", true);
                        data.spawn_colored_particles = JsonHelper.getBoolean(json, "spawn_colored_particles", false);
                        JsonObject colors = JsonHelper.getObject(json, "particle_colors", new JsonObject());
                        data.particle_colors = new ItemBehaviorData.ParticleColors(
                                JsonHelper.getInt(colors, "red", 0),
                                JsonHelper.getInt(colors, "green", 0),
                                JsonHelper.getInt(colors, "blue", 0)
                        );

                        boolean override_vanilla = JsonHelper.getBoolean(json, "override_vanilla",  false);
                        boolean complement = JsonHelper.getBoolean(json, "complement",  true);
                        int cooldown_time = JsonHelper.getInt(json, "cooldown", 50);

                        for (Item item : items) {
                            ItemBehaviorManager.addBehavior(item, ItemBehaviorAdder.dataPack(data), complement);
                            if (override_vanilla) ItemBehaviorManager.overrideVanilla(item);

                            if (cooldown_time != 50) {
                                ItemBehaviorManager.addCustomCooldown(item, cooldown_time);
                            }
                        }
                    } catch (IOException e) {
                        AndromedaLog.error("Error while parsing JSON for am_item_drop_behavior", e);
                    }
                }
                AndromedaLog.devInfo("Successfully loaded am_item_throw_behavior");
            }
        });

        AndromedaLog.info("ResourceConditionRegistry init complete!");
    }

    private static Item parseItemFromId(String id) {
        Item item = Registry.ITEM.get(Identifier.tryParse(id));
        if (item == Items.AIR) {
            throw new InvalidIdentifierException(String.format("(Andromeda) invalid identifier provided! %s", item));
        }
        return item;
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

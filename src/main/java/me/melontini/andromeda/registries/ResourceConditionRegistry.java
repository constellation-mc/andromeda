package me.melontini.andromeda.registries;

import com.google.gson.*;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorAdder;
import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.data.EggProcessingData;
import me.melontini.andromeda.util.data.ItemBehaviorData;
import me.melontini.andromeda.util.data.PlantData;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
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
                        List<String> fields = Arrays.stream(configOption.split("\\.")).toList();

                        try {
                            if (fields.size() > 1) {//🤯🤯🤯
                                Object obj = AndromedaConfig.class.getField(fields.get(0)).get(Andromeda.CONFIG);
                                for (int i = 1; i < (fields.size() - 1); i++) {
                                    obj = obj.getClass().getField(fields.get(i)).get(obj);
                                }
                                load = obj.getClass().getField(fields.get(1)).getBoolean(obj);
                            } else {
                                load = Andromeda.CONFIG.getClass().getField(configOption).getBoolean(Andromeda.CONFIG);
                            }
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
                var map = manager.findResources("am_crop_temperatures", identifier -> identifier.endsWith(".json"));
                for (Identifier entry : map) {
                    try {
                        var jsonElement = JsonHelper.deserialize(new InputStreamReader(manager.getResource(entry).getInputStream()));
                        //LogUtil.devInfo(jsonElement);
                        PlantData data = GSON.fromJson(jsonElement, PlantData.class);

                        if (Registry.BLOCK.get(Identifier.tryParse(data.identifier)) == Blocks.AIR) {
                            throw new InvalidIdentifierException(String.format("(Andromeda) invalid identifier provided! %s", data.identifier));
                        }

                        Andromeda.PLANT_DATA.putIfAbsent(Registry.BLOCK.get(Identifier.tryParse(data.identifier)), data);
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

                var map = manager.findResources("am_egg_processing", identifier -> identifier.endsWith(".json"));
                for (Identifier entry : map) {
                    try {
                        var jsonElement = JsonHelper.deserialize(new InputStreamReader(manager.getResource(entry).getInputStream()));
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
                ItemBehaviorAdder.addDefaults();
                var map = manager.findResources("am_item_throw_behavior", identifier -> identifier.endsWith(".json"));
                for (Identifier entry : map) {
                    try {
                        JsonObject json = JsonHelper.deserialize(new InputStreamReader(manager.getResource(entry).getInputStream()));
                        ItemBehaviorData data = new ItemBehaviorData();

                        Set<Item> items = new HashSet<>();

                        if (!json.has("item_id")) {
                            throw new InvalidIdentifierException("(Andromeda) missing item_id!");
                        }
                        JsonElement element = json.get("item_id");
                        if (element.isJsonArray()) {
                            for (JsonElement e : element.getAsJsonArray()) {
                                Item item = Registry.ITEM.get(Identifier.tryParse(e.getAsString()));
                                if (item == Items.AIR) {
                                    throw new InvalidIdentifierException(String.format("(Andromeda) invalid identifier provided! %s", item));
                                }
                                items.add(item);
                            }
                        } else {
                            Item item = Registry.ITEM.get(Identifier.tryParse(element.getAsString()));
                            if (item == Items.AIR) {
                                throw new InvalidIdentifierException(String.format("(Andromeda) invalid identifier provided! %s", item));
                            }
                            items.add(item);
                        }

                        data.on_entity_hit = new ItemBehaviorData.CommandHolder();
                        readCommands(JsonHelper.getObject(json, "on_entity_hit",  new JsonObject()), data.on_entity_hit);

                        data.on_block_hit = new ItemBehaviorData.CommandHolder();
                        readCommands(JsonHelper.getObject(json, "on_block_hit",  new JsonObject()), data.on_block_hit);

                        data.on_miss = new ItemBehaviorData.CommandHolder();
                        readCommands(JsonHelper.getObject(json, "on_miss",  new JsonObject()), data.on_miss);

                        data.on_any_hit = new ItemBehaviorData.CommandHolder();
                        readCommands(JsonHelper.getObject(json, "on_any_hit",  new JsonObject()), data.on_any_hit);

                        data.spawn_item_particles = JsonHelper.getBoolean(json, "spawn_item_particles", true);

                        data.spawn_colored_particles = JsonHelper.getBoolean(json, "spawn_colored_particles", false);

                        JsonObject colors = JsonHelper.getObject(json, "particle_colors", new JsonObject());
                        data.particle_colors = new ItemBehaviorData.ParticleColors();
                        data.particle_colors.red = JsonHelper.getInt(colors, "red", 0);
                        data.particle_colors.green = JsonHelper.getInt(colors, "green", 0);
                        data.particle_colors.blue = JsonHelper.getInt(colors, "blue", 0);

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

    private static void readCommands(JsonObject json, ItemBehaviorData.CommandHolder holder) {
        var item_arr = JsonHelper.getArray(json, "item_commands", null);
        if (item_arr != null) {
            List<String> item_commands = new ArrayList<>(item_arr.size());
            for (JsonElement element : item_arr) {
                item_commands.add(element.getAsString());
            }
            holder.item_commands = item_commands.toArray(String[]::new);
        }


        var user_arr = JsonHelper.getArray(json, "user_commands", null);
        if (user_arr != null) {
            List<String> user_commands = new ArrayList<>(user_arr.size());
            for (JsonElement element : user_arr) {
                user_commands.add(element.getAsString());
            }
            holder.user_commands = user_commands.toArray(String[]::new);
        }

        var server_arr = JsonHelper.getArray(json, "server_commands", null);
        if (server_arr != null) {
            List<String> server_commands = new ArrayList<>(server_arr.size());
            for (JsonElement element : server_arr) {
                server_commands.add(element.getAsString());
            }
            holder.server_commands = server_commands.toArray(String[]::new);
        }

        var hit_entity_arr = JsonHelper.getArray(json, "hit_entity_commands", null);
        if (hit_entity_arr != null) {
            List<String> server_commands = new ArrayList<>(hit_entity_arr.size());
            for (JsonElement element : hit_entity_arr) {
                server_commands.add(element.getAsString());
            }
            holder.hit_entity_commands = server_commands.toArray(String[]::new);
        }

        var hit_block_arr = JsonHelper.getArray(json, "hit_block_commands", null);
        if (hit_block_arr != null) {
            List<String> server_commands = new ArrayList<>(hit_block_arr.size());
            for (JsonElement element : hit_block_arr) {
                server_commands.add(element.getAsString());
            }
            holder.hit_block_commands = server_commands.toArray(String[]::new);
        }
    }
}

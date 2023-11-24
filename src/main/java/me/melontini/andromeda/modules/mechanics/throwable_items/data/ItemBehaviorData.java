package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.registries.ResourceRegistry.parseFromId;

public class ItemBehaviorData {
    public CommandHolder on_entity_hit;
    public CommandHolder on_block_hit;
    public CommandHolder on_miss;
    public CommandHolder on_any_hit;
    public boolean spawn_item_particles;
    public boolean spawn_colored_particles;
    public ParticleColors particle_colors;

    public record ParticleColors(int red, int green, int blue) {
    }

    public record CommandHolder(List<String> item_commands, List<String> user_commands, List<String> server_commands,
                                List<String> hit_entity_commands, List<String> hit_block_commands) {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ItemBehaviorManager.clear();
        });

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return id("am_item_throw_behavior");
            }

            @Override
            public void reload(ResourceManager manager) {
                ItemBehaviorManager.clear();
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
                            element.getAsJsonArray().forEach(e -> items.add(parseFromId(e.getAsString(), Registries.ITEM)));
                        } else {
                            items.add(parseFromId(element.getAsString(), Registries.ITEM));
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

package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.melontini.dark_matter.api.base.util.classes.Tuple;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.*;

import static me.melontini.andromeda.registries.ResourceRegistry.parseFromId;

public class ItemBehaviorData {

    public static final ItemBehaviorData DEFAULT = new ItemBehaviorData();

    public boolean override_vanilla = false;
    public boolean complement = true;
    public int cooldown_time = 50;

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

    public static Tuple<Set<Item>, ItemBehaviorData> create(JsonObject object) {
        ItemBehaviorData data = new ItemBehaviorData();

        Set<Item> items = new HashSet<>();

        if (!object.has("item_id")) throw new InvalidIdentifierException("(Andromeda) missing item_id!");

        JsonElement element = object.get("item_id");
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(e -> items.add(parseFromId(e.getAsString(), Registry.ITEM)));
        } else {
            items.add(parseFromId(element.getAsString(), Registry.ITEM));
        }
        if (items.isEmpty()) return Tuple.of(Collections.emptySet(), ItemBehaviorData.DEFAULT);

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

        data.override_vanilla = JsonHelper.getBoolean(object, "override_vanilla",  false);
        data.complement = JsonHelper.getBoolean(object, "complement",  true);
        data.cooldown_time = JsonHelper.getInt(object, "cooldown_time",  50);

        return Tuple.of(items, data);
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ItemBehaviorManager.clear());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new BehaviorLoader());
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

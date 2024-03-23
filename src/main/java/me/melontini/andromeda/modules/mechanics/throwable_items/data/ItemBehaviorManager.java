package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.CustomLog;
import lombok.Getter;
import me.melontini.andromeda.common.util.JsonDataLoader;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.Event;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.events.EventType;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.*;

import static me.melontini.andromeda.common.registries.Common.id;

@CustomLog
public class ItemBehaviorManager extends JsonDataLoader {

    public static final Identifier RELOADER_ID = id("item_throw_behaviors");

    public static ItemBehaviorManager get(MinecraftServer server) {
        return MakeSure.notNull(server).am$getReloader(RELOADER_ID);
    }

    public ItemBehaviorManager() {
        super(RELOADER_ID);
    }

    private final Map<Item, Holder> itemBehaviors = new IdentityHashMap<>();
    private final Object2IntOpenHashMap<Item> customCooldowns = Utilities.consume(new Object2IntOpenHashMap<>(), map -> {
        map.defaultReturnValue(50);
    });
    private final Set<Item> overrideVanilla = new HashSet<>();
    private final Set<Item> disabled = new HashSet<>();

    private static final Map<Item, Holder> STATIC = new IdentityHashMap<>();

    public static void register(Event behavior, Item... items) {
        register(behavior, Arrays.asList(items));
    }

    public static void register(Event behavior, Collection<Item> items) {
        for (Item item : items) {
            Holder holder = STATIC.computeIfAbsent(item, Holder::new);
            holder.addBehavior(behavior, true);
        }
    }

    public List<Event> getBehaviors(Item item, EventType type) {
        Holder holder = itemBehaviors.get(item);
        if (holder == null) return Collections.emptyList();
        return Collections.unmodifiableList(holder.behaviors.getOrDefault(type, Collections.emptyList()));
    }

    public void addBehavior(Item item, Event behavior, boolean complement) {
        if (disabled.contains(item)) return;

        Holder holder = itemBehaviors.computeIfAbsent(item, Holder::new);
        holder.addBehavior(behavior, complement);
    }

    public void addBehavior(Item item, Event behavior) {
        addBehavior(item, behavior, true);
    }

    public void addBehaviors(Event behavior, boolean complement, Item... items) {
        for (Item item : items) addBehavior(item, behavior, complement);
    }

    public void addBehaviors(Event behavior, Item... items) {
        for (Item item : items) addBehavior(item, behavior);
    }

    public void disable(Item item) {
        disabled.add(item);
        itemBehaviors.remove(item);
    }

    public boolean hasBehaviors(Item item) {
        return itemBehaviors.containsKey(item);
    }

    public void clear() {
        itemBehaviors.clear();
        customCooldowns.clear();
        overrideVanilla.clear();
        disabled.clear();
    }

    public Set<Item> itemsWithBehaviors() {
        return Collections.unmodifiableSet(itemBehaviors.keySet());
    }

    public void overrideVanilla(Item item) {
        overrideVanilla.add(item);
    }

    public boolean overridesVanilla(Item item) {
        return overrideVanilla.contains(item);
    }

    public void addCustomCooldown(Item item, int cooldown) {
        customCooldowns.putIfAbsent(item, cooldown);
    }

    public void replaceCustomCooldown(Item item, int cooldown) {
        customCooldowns.put(item, cooldown);
    }

    public int getCooldown(Item item) {
        return customCooldowns.getInt(item);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        this.clear();
        STATIC.forEach((item, holder) -> this.itemBehaviors.put(item, new Holder(item, holder.behaviors)));

        Maps.transformValues(prepared, input -> ItemBehaviorData.CODEC.parse(JsonOps.INSTANCE, input).getOrThrow(false, string -> {
            throw new RuntimeException(string);
        })).forEach((id, data) -> {
            if (data.items().isEmpty()) return;

            for (Item item : data.items()) {
                if (data.disabled()) {
                    this.disable(item);
                    continue;
                }

                for (Event event : data.events()) {
                    this.addBehavior(item, event, data.complement());
                }
                if (data.override_vanilla()) this.overrideVanilla(item);

                data.cooldown().ifPresent(integer -> this.addCustomCooldown(item, integer));
            }
        });
    }

    private static class Holder {
        final Map<EventType, List<Event>> behaviors;
        @Getter
        private final Item item;
        private boolean locked;

        public Holder(Item item) {
            this(item, Collections.emptyMap());
        }

        public Holder(Item item, Map<EventType, List<Event>> behaviors) {
            this.item = item;
            this.behaviors = new HashMap<>(behaviors);
        }

        public void addBehavior(Event behavior, boolean complement) {
            if (!this.locked) {
                if (!complement) this.behaviors.clear();
                this.behaviors.computeIfAbsent(behavior.type(), type -> new ArrayList<>()).add(behavior);
                if (!complement) this.locked = true;
            }
        }
    }
}

package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import me.melontini.andromeda.common.util.IdentifiedJsonDataLoader;
import me.melontini.andromeda.modules.mechanics.throwable_items.ItemBehavior;
import me.melontini.commander.api.expression.Arithmetica;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.data.loading.ReloaderType;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.*;

import static me.melontini.andromeda.common.registries.Common.id;

public class ItemBehaviorManager extends IdentifiedJsonDataLoader {

    public static final ReloaderType<ItemBehaviorManager> RELOADER = ReloaderType.create(id("item_throw_behaviors"));

    public ItemBehaviorManager() {
        super(RELOADER.identifier());
    }

    private final IdentityHashMap<Item, Holder> itemBehaviors = new IdentityHashMap<>();
    private final Object2ObjectMap<Item, Arithmetica> customCooldowns = Utilities.supply(new Object2ObjectOpenHashMap<>(), map -> {
        map.defaultReturnValue(Arithmetica.constant(50));
    });
    private final Set<Item> overrideVanilla = new HashSet<>();
    private final Set<Item> disabled = new HashSet<>();

    private static final IdentityHashMap<Item, Holder> STATIC = new IdentityHashMap<>();

    public static void register(ItemBehavior behavior, Item... items) {
        register(behavior, Arrays.asList(items));
    }

    public static void register(ItemBehavior behavior, Collection<Item> items) {
        for (Item item : items) {
            Holder holder = STATIC.computeIfAbsent(item, Holder::new);
            holder.addBehavior(behavior, true);
        }
    }

    public List<ItemBehavior> getBehaviors(Item item) {
        Holder holder = itemBehaviors.get(item);
        if (holder == null) return Collections.emptyList();
        return Collections.unmodifiableList(holder.behaviors);
    }

    public void addBehavior(Item item, ItemBehavior behavior, boolean complement) {
        if (disabled.contains(item)) return;

        Holder holder = itemBehaviors.computeIfAbsent(item, Holder::new);
        holder.addBehavior(behavior, complement);
    }

    public void addBehavior(Item item, ItemBehavior behavior) {
        addBehavior(item, behavior, true);
    }

    public void addBehaviors(ItemBehavior behavior, boolean complement, Item... items) {
        for (Item item : items) addBehavior(item, behavior, complement);
    }

    public void addBehaviors(ItemBehavior behavior, Item... items) {
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

    public void addCustomCooldown(Item item, Arithmetica cooldown) {
        customCooldowns.putIfAbsent(item, cooldown);
    }
    public void replaceCustomCooldown(Item item, Arithmetica cooldown) {
        customCooldowns.put(item, cooldown);
    }
    public Arithmetica getCooldown(Item item) {
        return customCooldowns.get(item);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        this.clear();
        itemBehaviors.putAll(STATIC);

        Maps.transformValues(data, input -> ItemBehaviorData.create(input.getAsJsonObject())).forEach((id, behaviorData) -> {
            if (behaviorData.parameters().items().isEmpty()) return;

            for (Item item : behaviorData.parameters().items()) {
                if (behaviorData.parameters().disabled()) {
                    this.disable(item);
                    continue;
                }

                this.addBehavior(item, behaviorData, behaviorData.parameters().complement());
                if (behaviorData.parameters().override_vanilla()) this.overrideVanilla(item);

                this.addCustomCooldown(item, behaviorData.parameters().cooldown());
            }
        });
    }

    private static class Holder {
        final List<ItemBehavior> behaviors = new ArrayList<>();
        @Getter
        private final Item item;
        private boolean locked;

        public Holder(Item item) {
            this.item = item;
        }

        public void addBehavior(ItemBehavior behavior, boolean complement) {
            if (!this.locked) {
                if (!complement) this.behaviors.clear();
                this.behaviors.add(behavior);
                if (!complement) this.locked = true;
            }
        }
    }
}

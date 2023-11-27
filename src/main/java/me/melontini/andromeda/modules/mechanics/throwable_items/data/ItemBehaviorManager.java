package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import me.melontini.andromeda.modules.mechanics.throwable_items.ItemBehavior;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.minecraft.item.Item;

import java.util.*;

public class ItemBehaviorManager {

    private static final Map<Item, Holder> ITEM_BEHAVIORS = new IdentityHashMap<>();
    private static final Object2IntOpenHashMap<Item> CUSTOM_COOLDOWNS = Utilities.consume(new Object2IntOpenHashMap<>(), map -> {
        map.defaultReturnValue(50);
    });
    private static final Set<Item> OVERRIDE_VANILLA = new HashSet<>();
    private static final Set<Item> DISABLED = new HashSet<>();

    public static List<ItemBehavior> getBehaviors(Item item) {
        Holder holder = ITEM_BEHAVIORS.get(item);
        if (holder == null) return Collections.emptyList();
        return Collections.unmodifiableList(holder.behaviors);
    }

    public static void addBehavior(Item item, ItemBehavior behavior, boolean complement) {
        if (DISABLED.contains(item)) return;

        Holder holder = ITEM_BEHAVIORS.computeIfAbsent(item, Holder::new);
        holder.addBehavior(behavior, complement);
    }

    public static void addBehavior(Item item, ItemBehavior behavior) {
        addBehavior(item, behavior, true);
    }

    public static void addBehaviors(ItemBehavior behavior, boolean complement, Item... items) {
        for (Item item : items) addBehavior(item, behavior, complement);
    }

    public static void addBehaviors(ItemBehavior behavior, Item... items) {
        for (Item item : items) addBehavior(item, behavior);
    }

    public static void disable(Item item) {
        DISABLED.add(item);
        ITEM_BEHAVIORS.remove(item);
    }

    public static boolean hasBehaviors(Item item) {
        return ITEM_BEHAVIORS.containsKey(item);
    }

    public static void clear() {
        ITEM_BEHAVIORS.clear();
        CUSTOM_COOLDOWNS.clear();
        OVERRIDE_VANILLA.clear();
        DISABLED.clear();
    }

    public static Set<Item> itemsWithBehaviors() {
        return Collections.unmodifiableSet(ITEM_BEHAVIORS.keySet());
    }

    public static void overrideVanilla(Item item) {
        OVERRIDE_VANILLA.add(item);
    }
    public static boolean overridesVanilla(Item item) {
        return OVERRIDE_VANILLA.contains(item);
    }

    public static void addCustomCooldown(Item item, int cooldown) {
        CUSTOM_COOLDOWNS.putIfAbsent(item, cooldown);
    }
    public static void replaceCustomCooldown(Item item, int cooldown) {
        CUSTOM_COOLDOWNS.put(item, cooldown);
    }
    public static int getCooldown(Item item) {
        return CUSTOM_COOLDOWNS.getInt(item);
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

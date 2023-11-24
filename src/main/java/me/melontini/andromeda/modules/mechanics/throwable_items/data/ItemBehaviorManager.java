package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.mechanics.throwable_items.ItemBehavior;
import me.melontini.andromeda.modules.mechanics.throwable_items.ThrowableItems;
import me.melontini.dark_matter.api.base.util.classes.Lazy;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import java.util.*;

public class ItemBehaviorManager {
    private static final Lazy<ThrowableItems> module = Lazy.of(() -> () -> ModuleManager.quick(ThrowableItems.class));

    private static final Map<Item, Holder> ITEM_BEHAVIORS = new IdentityHashMap<>();
    private static final Object2IntOpenHashMap<Item> CUSTOM_COOLDOWNS = new Object2IntOpenHashMap<>();
    private static final Set<Item> OVERRIDE_VANILLA = new HashSet<>();

    public static List<ItemBehavior> getBehaviors(Item item) {
        if (module.get().config().blacklist.contains(Registries.ITEM.getId(item).toString()))
            return Collections.emptyList();
        Holder holder = ITEM_BEHAVIORS.get(item);
        if (holder == null) return Collections.emptyList();
        return Collections.unmodifiableList(holder.behaviors);
    }

    public static void addBehavior(Item item, ItemBehavior behavior, boolean complement) {
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

    public static boolean hasBehaviors(Item item) {
        return ITEM_BEHAVIORS.containsKey(item) && !module.get().config().blacklist.contains(Registries.ITEM.getId(item).toString());
    }

    public static void clear() {
        ITEM_BEHAVIORS.clear();
        CUSTOM_COOLDOWNS.clear();
        OVERRIDE_VANILLA.clear();
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
        return CUSTOM_COOLDOWNS.getOrDefault(item, 50);
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

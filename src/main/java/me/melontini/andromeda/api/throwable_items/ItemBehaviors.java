package me.melontini.andromeda.api.throwable_items;

import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import net.minecraft.item.Item;

/**
 * Meant to be used with the {@code andromeda:collect_behaviors} entrypoint, implementing the {@link java.lang.Runnable} interface.
 */
public final class ItemBehaviors {

    public static void addBehavior(Item item, ItemBehavior behavior, boolean complement) {
        ItemBehaviorManager.addBehavior(item, behavior, complement);
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
}

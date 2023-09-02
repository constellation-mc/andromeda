package me.melontini.andromeda.api;

import me.melontini.andromeda.content.throwable_items.ItemBehaviorManager;
import me.melontini.andromeda.entity.FlyingItemEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Meant to be used with the {@code andromeda:collect_behaviors} entrypoint, implementing the {@link java.lang.Runnable} interface.
 */
public final class ItemBehaviors {

    public static void addBehavior(Item item, Behavior behavior, boolean complement) {
        ItemBehaviorManager.addBehavior(item, behavior, complement);
    }
    public static void addBehavior(Item item, Behavior behavior) {
        addBehavior(item, behavior, true);
    }

    public static void addBehaviors(Behavior behavior, boolean complement, Item... items) {
        for (Item item : items) addBehavior(item, behavior, complement);
    }

    public static void addBehaviors(Behavior behavior, Item... items) {
        for (Item item : items) addBehavior(item, behavior);
    }

    @FunctionalInterface
    public interface Behavior {

        void onCollision(ItemStack stack, FlyingItemEntity flyingItemEntity, World world, @Nullable Entity user, HitResult hitResult);

    }
}

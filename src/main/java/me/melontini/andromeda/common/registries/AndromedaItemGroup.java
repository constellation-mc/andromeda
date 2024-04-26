package me.melontini.andromeda.common.registries;

import me.melontini.andromeda.base.Module;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class AndromedaItemGroup {

    private static final List<Consumer<Acceptor>> ACCEPTORS = new ArrayList<>();

    public static List<Consumer<Acceptor>> getAcceptors() {
        return Collections.unmodifiableList(ACCEPTORS);
    }

    public static void accept(Consumer<Acceptor> consumer) {
        ACCEPTORS.add(consumer);
    }

    public interface Acceptor {
        void stack(Module<?> module, ItemStack stack);

        default void stacks(Module<?> module, List<ItemStack> stacks) {
            for (ItemStack stack : stacks) {
                stack(module, stack);
            }
        }

        default <T extends ItemConvertible> void items(Module<?> module, List<T> items) {
            stacks(module, items.stream().map(ItemStack::new).toList());
        }

        default <T extends ItemConvertible> void item(Module<?> module, T item) {
            stack(module, new ItemStack(item));
        }

        default <T extends ItemConvertible> void keepers(Module<?> module, List<Keeper<? extends ItemConvertible>> keepers) {
            stacks(module, keepers.stream().filter(Keeper::isPresent).map(Keeper::orThrow).map(ItemStack::new).toList());
        }

        default <T extends ItemConvertible> void keeper(Module<?> module, Keeper<T> keeper) {
            if (keeper.isPresent()) stack(module, new ItemStack(keeper.orThrow()));
        }
    }
}

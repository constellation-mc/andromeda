package me.melontini.andromeda.common.registries;

import me.melontini.andromeda.base.Module;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.*;
import java.util.function.Consumer;

import static me.melontini.andromeda.common.registries.Common.id;

public class AndromedaItemGroup {

    private static final Set<Consumer<Acceptor>> ACCEPTORS = new LinkedHashSet<>();

    @SuppressWarnings("unused")
    public static final ItemGroup GROUP = ContentBuilder.ItemGroupBuilder.create(id("group"))
            .entries(entries -> {
                Map<Module<?>, List<ItemStack>> stackMap = new LinkedHashMap<>();
                Acceptor acceptor = (module, stack) -> {
                    if (!stack.isEmpty()) {
                        stackMap.computeIfAbsent(module, module1 -> new ArrayList<>()).add(stack);
                    }
                };
                ACCEPTORS.forEach(consumer -> consumer.accept(acceptor));

                Map<Module<?>, List<ItemStack>> small = new LinkedHashMap<>();
                Map<Module<?>, List<ItemStack>> big = new LinkedHashMap<>();

                if (stackMap.isEmpty()) {
                    entries.add(Items.BARRIER);
                    return;
                }

                stackMap.forEach((module, itemStacks) -> {
                    if (itemStacks.size() > 2) {
                        big.put(module, itemStacks);
                    } else if (!itemStacks.isEmpty()) {
                        small.put(module, itemStacks);
                    }
                });

                if (small.isEmpty() && big.isEmpty()) {
                    entries.add(Items.BARRIER);
                    return;
                }

                List<ItemStack> stacks = new ArrayList<>();
                small.forEach((m, itemStacks) -> {
                    ItemStack sign = new ItemStack(Items.SPRUCE_SIGN);
                    sign.setCustomName(TextUtil.translatable("config.andromeda.%s".formatted(m.meta().dotted())));
                    stacks.add(sign);
                    stacks.addAll(itemStacks);
                    stacks.add(ItemStack.EMPTY);
                });
                entries.appendStacks(stacks);

                big.forEach((m, itemStacks) -> {
                    ItemStack sign = new ItemStack(Items.SPRUCE_SIGN);
                    sign.setCustomName(TextUtil.translatable("config.andromeda.%s".formatted(m.meta().dotted())));
                    itemStacks.add(0, sign);
                    entries.appendStacks(itemStacks);
                });
            })
            .displayName(TextUtil.translatable("itemGroup.andromeda.items")).optional().orElseThrow();

    public static void init() {

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
            stacks(module, keepers.stream().filter(Keeper::isPresent).map(Keeper::get).map(ItemStack::new).toList());
        }

        default <T extends ItemConvertible> void keeper(Module<?> module, Keeper<T> keeper) {
            if (keeper.isPresent()) stack(module, new ItemStack(keeper.get()));
        }
    }
}

package me.melontini.andromeda.common.registries;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.AndromedaTexts;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.interfaces.DarkMatterEntries;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;
import java.util.function.Consumer;

import static me.melontini.andromeda.common.registries.Common.id;
import static me.melontini.andromeda.common.registries.Common.start;

public class AndromedaItemGroup {

    private static final Set<Consumer<Acceptor>> ACCEPTORS = new LinkedHashSet<>();

    @SuppressWarnings("unused")
    public static final Keeper<ItemGroup> GROUP = start(() -> ContentBuilder.ItemGroupBuilder.create(id("group"))
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
                    } else if (!itemStacks.isEmpty()){
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
                appendStacks(entries, stacks);

                big.forEach((m, itemStacks) -> {
                    ItemStack sign = new ItemStack(Items.SPRUCE_SIGN);
                    sign.setCustomName(TextUtil.translatable("config.andromeda.%s".formatted(m.meta().dotted())));
                    itemStacks.add(0, sign);
                    appendStacks(entries, itemStacks);
                });
            })
            .displayName(AndromedaTexts.ITEM_GROUP_NAME));

    private static void appendStacks(DarkMatterEntries stacks, Collection<ItemStack> list) {
        if (list == null || list.isEmpty()) return; //we shouldn't add line breaks if there are no items.

        int rows = MathStuff.fastCeil(list.size() / 9d);
        stacks.addAll(list, DarkMatterEntries.Visibility.TAB);
        int left = (rows * 9) - list.size();
        for (int i = 0; i < left; i++) {
            stacks.add(ItemStack.EMPTY, DarkMatterEntries.Visibility.TAB); //fill the gaps
        }
        stacks.addAll(DefaultedList.ofSize(9, ItemStack.EMPTY), DarkMatterEntries.Visibility.TAB); //line break
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

        default <T extends ItemConvertible> void keepers(Module<?> module, List<Keeper<T>> keepers) {
            stacks(module, keepers.stream().filter(Keeper::isPresent).map(Keeper::get).map(ItemStack::new).toList());
        }
        default <T extends ItemConvertible> void keeper(Module<?> module, Keeper<T> keeper) {
            if (keeper.isPresent()) stack(module, new ItemStack(keeper.get()));
        }
    }
}

package me.melontini.andromeda.common.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleHelper;
import me.melontini.andromeda.bootstrap.event.bus.Bus;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.dark_matter.api.item_group.ItemGroupBuilder;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public interface AndromedaItemGroup {

  Bus<AndromedaItemGroup> BUS = Bus.create(AndromedaItemGroup.class, events -> acceptor -> {
    for (AndromedaItemGroup event : events) {
      event.onCreateItemGroup(acceptor);
    }
  });

  void onCreateItemGroup(Acceptor acceptor);

  static CreativeModeTab create() {
    return ItemGroupBuilder.create(Andromeda.id("group"))
        .entries(entries -> {
          Map<Module, List<ItemStack>> stackMap = new LinkedHashMap<>();
          AndromedaItemGroup.Acceptor acceptor = (module, main, stack) -> {
            if (!stack.isEmpty()) {
              stackMap.computeIfAbsent(module, module1 -> new ArrayList<>()).add(stack);
            }
          };
          BUS.invoker().onCreateItemGroup(acceptor);
          Map<Module, List<ItemStack>> small = new LinkedHashMap<>();
          Map<Module, List<ItemStack>> big = new LinkedHashMap<>();
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
            sign.setHoverName(
                TextUtil.translatable("config.andromeda.%s".formatted(ModuleHelper.dotted(m))));
            stacks.add(sign);
            stacks.addAll(itemStacks);
            stacks.add(ItemStack.EMPTY);
          });
          entries.appendStacks(stacks);
          big.forEach((m, itemStacks) -> {
            ItemStack sign = new ItemStack(Items.SPRUCE_SIGN);
            sign.setHoverName(
                TextUtil.translatable("config.andromeda.%s".formatted(ModuleHelper.dotted(m))));
            itemStacks.add(0, sign);
            entries.appendStacks(itemStacks);
          });
        })
        .displayName(TextUtil.translatable("itemGroup.andromeda.items"))
        .build();
  }

  interface Acceptor {
    void stack(Module module, ResourceKey<CreativeModeTab> main, ItemStack stack);

    default void stacks(Module module, ResourceKey<CreativeModeTab> main, List<ItemStack> stacks) {
      for (ItemStack stack : stacks) {
        stack(module, main, stack);
      }
    }

    default <T extends ItemLike> void items(
        Module module, ResourceKey<CreativeModeTab> main, List<T> items) {
      stacks(module, main, items.stream().map(ItemStack::new).toList());
    }

    default <T extends ItemLike> void item(
        Module module, ResourceKey<CreativeModeTab> main, T item) {
      stack(module, main, new ItemStack(item));
    }

    default <T extends ItemLike> void keepers(
        Module module,
        ResourceKey<CreativeModeTab> main,
        List<Keeper<? extends ItemLike>> keepers) {
      stacks(
          module,
          main,
          keepers.stream()
              .filter(Keeper::isPresent)
              .map(Keeper::orThrow)
              .map(ItemStack::new)
              .toList());
    }

    default <T extends ItemLike> void keeper(
        Module module, ResourceKey<CreativeModeTab> main, Keeper<T> keeper) {
      if (keeper.isPresent()) stack(module, main, new ItemStack(keeper.orThrow()));
    }
  }
}

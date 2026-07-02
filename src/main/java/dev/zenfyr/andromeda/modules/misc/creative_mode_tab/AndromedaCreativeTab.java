package dev.zenfyr.andromeda.modules.misc.creative_mode_tab;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.pulsar.api.event.Bus;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface AndromedaCreativeTab {

  Bus<AndromedaCreativeTab> BUS = Bus.create(AndromedaCreativeTab.class, events -> acceptor -> {
    for (AndromedaCreativeTab event : events) {
      event.onCreateCreativeTab(acceptor);
    }
  });

  void onCreateCreativeTab(Acceptor acceptor);

  interface Acceptor {
    void stack(Module module, ItemStack stack);

    default void stacks(Module module, List<ItemStack> stacks) {
      for (ItemStack stack : stacks) {
        stack(module, stack);
      }
    }

    default <T extends ItemLike> void items(Module module, List<T> items) {
      stacks(module, items.stream().map(ItemStack::new).toList());
    }

    default <T extends ItemLike> void item(Module module, T item) {
      stack(module, new ItemStack(item));
    }

    default <T extends ItemLike> void keepers(
        Module module, List<Keeper<? extends ItemLike>> keepers) {
      stacks(
          module,
          keepers.stream()
              .filter(Keeper::isPresent)
              .map(Keeper::orThrow)
              .map(ItemStack::new)
              .toList());
    }

    default <T extends ItemLike> void keeper(Module module, Keeper<T> keeper) {
      if (keeper.isPresent()) stack(module, new ItemStack(keeper.orThrow()));
    }
  }
}

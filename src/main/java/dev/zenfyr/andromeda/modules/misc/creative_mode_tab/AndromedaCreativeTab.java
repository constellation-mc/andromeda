package dev.zenfyr.andromeda.modules.misc.creative_mode_tab;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.pulsar.api.event.Bus;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
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

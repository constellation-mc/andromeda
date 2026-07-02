package dev.zenfyr.andromeda.modules.misc.creative_mode_tab;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleHelper;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.pulsar.api.creativetab.CreativeModeTabBuilder;
import dev.zenfyr.pulsar.api.creativetab.PulsarEntries;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CreativeModeTabMain {

  public static final Keeper<CreativeModeTab> TAB = Keeper.create();

  public static void init() {
    CreativeModeTabMain.TAB.init(CreativeModeTabBuilder.create(Andromeda.id("group"))
        .entries(entries -> {
          Map<dev.zenfyr.andromeda.bootstrap.Module, List<ItemStack>> stackMap =
              new LinkedHashMap<>();
          AndromedaCreativeTab.Acceptor acceptor = (module, main, stack) -> {
            if (!stack.isEmpty()) {
              stackMap.computeIfAbsent(module, module1 -> new ArrayList<>()).add(stack);
            }
          };
          AndromedaCreativeTab.BUS.invoker().onCreateCreativeTab(acceptor);
          Map<dev.zenfyr.andromeda.bootstrap.Module, List<ItemStack>> small = new LinkedHashMap<>();
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

            entries.addAll(itemStacks, PulsarEntries.Visibility.SEARCH);
            stacks.add(sign);
            stacks.addAll(itemStacks);
            stacks.add(ItemStack.EMPTY);
          });
          entries.appendStacks(stacks);
          big.forEach((m, itemStacks) -> {
            ItemStack sign = new ItemStack(Items.SPRUCE_SIGN);
            sign.setHoverName(
                TextUtil.translatable("config.andromeda.%s".formatted(ModuleHelper.dotted(m))));

            entries.addAll(itemStacks, PulsarEntries.Visibility.SEARCH);
            entries.appendStacks(
                Stream.concat(Stream.of(sign), itemStacks.stream()).toList());
          });
        })
        .icon(() -> Items.AMETHYST_BLOCK)
        .displayName(TextUtil.translatable("itemGroup.andromeda.items"))
        .build());
  }
}

package me.melontini.andromeda.modules.blocks.better_fletching_table;

import static me.melontini.andromeda.common.Andromeda.id;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.*;
import java.util.function.Function;
import me.melontini.andromeda.base.Bootstrap;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class FletchingScreenHandler extends ForgingScreenHandler {

  public static final Keeper<ScreenHandlerType<FletchingScreenHandler>> FLETCHING = Keeper.create();
  public static final Keeper<ComponentType<Integer>> TIGHTENED = Keeper.create();

  public FletchingScreenHandler(int syncId, PlayerInventory playerInventory) {
    this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
  }

  public FletchingScreenHandler(
      int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
    super(FLETCHING.orThrow(), syncId, playerInventory, context);
  }

  @Override
  public boolean canTakeOutput(PlayerEntity player, boolean present) {
    return !this.output.isEmpty();
  }

  @Override
  protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
    stack.onCraftByPlayer(player.getWorld(), player, stack.getCount());
    this.output.unlockLastRecipe(player, List.of(this.input.getStack(0), this.input.getStack(1)));
    this.decrementStack(0);
    this.decrementStack(1);
    this.context.run((world, pos) -> world.syncWorldEvent(1044, pos, 0));
  }

  private void decrementStack(int slot) {
    ItemStack itemStack = this.input.getStack(slot);
    itemStack.decrement(1);
    this.input.setStack(slot, itemStack);
  }

  // This should probably be data-driven, but whatever.
  private static final Map<Ingredient, Map<Ingredient, Function<ItemStack, ItemStack>>> RECIPES =
      new HashMap<>();

  public static void addRecipe(
      Function<ItemStack, ItemStack> consumer, Ingredient ingredient, Ingredient input) {
    RECIPES.computeIfAbsent(input, i -> new IdentityHashMap<>()).put(ingredient, consumer);
  }

  @Override
  public void updateResult() {
    ItemStack stack = getSlot(0).getStack();

    var lookup = RECIPES.entrySet().stream()
        .filter(e -> e.getKey().test(stack))
        .flatMap(e -> e.getValue().entrySet().stream())
        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    if (lookup.isEmpty()) {
      getSlot(2).setStack(ItemStack.EMPTY);
      return;
    }

    ItemStack stack1 = getSlot(1).getStack();
    var recipe = lookup.entrySet().stream().filter(e -> e.getKey().test(stack1)).findFirst();
    if (recipe.isEmpty()) {
      getSlot(2).setStack(ItemStack.EMPTY);
      return;
    }

    getSlot(2).setStack(recipe.get().getValue().apply(stack));
  }

  @Override
  protected ForgingSlotsManager getForgingSlotsManager() {
    return ForgingSlotsManager.create()
        .input(0, 27, 47, stack -> true)
        .input(1, 76, 47, stack -> true)
        .output(0, 134, 47)
        .build();
  }

  @Override
  protected boolean canUse(BlockState state) {
    return state.isOf(Blocks.FLETCHING_TABLE);
  }

  @Override
  public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
    return slot.inventory != this.output && super.canInsertIntoSlot(stack, slot);
  }

  static void init(BetterFletchingTable module) {
    FLETCHING.init(RegistryUtil.register(
        Registries.SCREEN_HANDLER,
        id("fletching"),
        RegistryUtil.screenHandlerType(FletchingScreenHandler::new)));
    TIGHTENED.init(RegistryUtil.register(
        Registries.DATA_COMPONENT_TYPE, id("tightened"), () -> ComponentType.<Integer>builder()
            .codec(Codec.INT)
            .packetCodec(PacketCodecs.VAR_INT)
            .build()));

    Set<Item> tightable = Sets.newHashSet(Items.BOW, Items.CROSSBOW);

    if (Bootstrap.isModLoaded(module, "additionaladditions")) {
      Registries.ITEM
          .getOrEmpty(Identifier.of("additionaladditions", "crossbow_with_spyglass"))
          .ifPresent(item -> {
            tightable.add(item);
            FletchingScreenHandler.addRecipe(
                stack -> stack.withItem(item),
                Ingredient.ofItems(Items.SPYGLASS),
                Ingredient.ofItems(Items.CROSSBOW));
          });
    }

    FletchingScreenHandler.addRecipe(
        stack -> {
          int i = stack.getOrDefault(TIGHTENED.get(), 0);
          if (i >= 32) return ItemStack.EMPTY;

          ItemStack newStack = stack.copy();
          newStack.set(TIGHTENED.get(), Math.min(i + 2, 32));
          return newStack;
        },
        Ingredient.ofItems(Items.STRING),
        Ingredient.ofItems(tightable.toArray(ItemConvertible[]::new)));
  }
}

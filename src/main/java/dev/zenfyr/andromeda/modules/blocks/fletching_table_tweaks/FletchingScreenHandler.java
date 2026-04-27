package dev.zenfyr.andromeda.modules.blocks.fletching_table_tweaks;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.andromeda.util.Debug;
import java.util.*;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FletchingScreenHandler extends ItemCombinerMenu {

  public static final Keeper<MenuType<FletchingScreenHandler>> FLETCHING = Keeper.create();
  public static final Keeper<DataComponentType<Integer>> TIGHTENED = Keeper.create();

  public FletchingScreenHandler(int syncId, Inventory playerInventory) {
    this(syncId, playerInventory, ContainerLevelAccess.NULL);
  }

  public FletchingScreenHandler(
      int syncId, Inventory playerInventory, ContainerLevelAccess context) {
    super(FLETCHING.orThrow(), syncId, playerInventory, context, createInputSlotDefinitions());
  }

  @Override
  public boolean mayPickup(Player player, boolean present) {
    return !this.resultSlots.isEmpty();
  }

  @Override
  protected void onTake(Player player, ItemStack stack) {
    stack.onCraftedBy(player, stack.getCount());
    this.resultSlots.awardUsedRecipes(
        player, List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1)));
    this.decrementStack(0);
    this.decrementStack(1);
    this.access.execute((world, pos) -> world.levelEvent(1044, pos, 0));
  }

  private void decrementStack(int slot) {
    ItemStack itemStack = this.inputSlots.getItem(slot);
    itemStack.shrink(1);
    this.inputSlots.setItem(slot, itemStack);
  }

  // This should probably be data-driven, but whatever.
  private static final Map<Ingredient, Map<Ingredient, Function<ItemStack, ItemStack>>> RECIPES =
      new HashMap<>();

  public static void addRecipe(
      Function<ItemStack, ItemStack> consumer, Ingredient ingredient, Ingredient input) {
    RECIPES.computeIfAbsent(input, i -> new IdentityHashMap<>()).put(ingredient, consumer);
  }

  @Override
  public void createResult() {
    ItemStack stack = getSlot(0).getItem();

    var lookup = RECIPES.entrySet().stream()
        .filter(e -> e.getKey().test(stack))
        .flatMap(e -> e.getValue().entrySet().stream())
        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    if (lookup.isEmpty()) {
      getSlot(2).setByPlayer(ItemStack.EMPTY);
      return;
    }

    ItemStack stack1 = getSlot(1).getItem();
    var recipe = lookup.entrySet().stream().filter(e -> e.getKey().test(stack1)).findFirst();
    if (recipe.isEmpty()) {
      getSlot(2).setByPlayer(ItemStack.EMPTY);
      return;
    }

    getSlot(2).setByPlayer(recipe.get().getValue().apply(stack));
  }

  private static ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
    return ItemCombinerMenuSlotDefinition.create()
        .withSlot(0, 27, 47, stack -> true)
        .withSlot(1, 76, 47, stack -> true)
        .withResultSlot(2, 134, 47)
        .build();
  }

  @Override
  protected boolean isValidBlock(BlockState state) {
    return state.is(Blocks.FLETCHING_TABLE);
  }

  @Override
  public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
    return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
  }

  static void init() {
    var module = ModuleManager.get().get(FletchingTableTweaks.class).orElseThrow();
    FletchingScreenHandler.FLETCHING.init(Registry.register(
        BuiltInRegistries.MENU,
        id("fletching"),
        new MenuType<>(FletchingScreenHandler::new, FeatureFlagSet.of())));
    TIGHTENED.init(Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        id("tightened"),
        DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .build()));

    Set<Item> tightable = Sets.newHashSet(Items.BOW, Items.CROSSBOW);

    if (Debug.get().isModLoaded(module, "additionaladditions")) {
      BuiltInRegistries.ITEM
          .getOptional(ResourceLocation.tryBuild("additionaladditions", "crossbow_with_spyglass"))
          .ifPresent(item -> {
            tightable.add(item);
            FletchingScreenHandler.addRecipe(
                stack -> stack.transmuteCopy(item),
                Ingredient.of(Items.SPYGLASS),
                Ingredient.of(Items.CROSSBOW));
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
        Ingredient.of(Items.STRING),
        Ingredient.of(tightable.toArray(ItemLike[]::new)));
  }
}

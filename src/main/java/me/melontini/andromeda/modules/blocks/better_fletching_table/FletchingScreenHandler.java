package me.melontini.andromeda.modules.blocks.better_fletching_table;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FletchingScreenHandler extends ForgingScreenHandler {

    public FletchingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public FletchingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(Content.FLETCHING.orThrow(), syncId, playerInventory, context);
    }

    @Override
    public boolean canTakeOutput(PlayerEntity player, boolean present) {
        return !this.output.isEmpty();
    }

    @Override
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
        stack.onCraft(player.world, player, stack.getCount());
        this.output.unlockLastRecipe(player);
        this.decrementStack(0);
        this.decrementStack(1);
    }

    private void decrementStack(int slot) {
        ItemStack itemStack = this.input.getStack(slot);
        itemStack.decrement(1);
        this.input.setStack(slot, itemStack);
    }

    //This should probably be data-driven, but whatever.
    private static final Map<Ingredient, Map<Ingredient, Function<ItemStack, ItemStack>>> RECIPES = new IdentityHashMap<>();

    public static void addRecipe(Function<ItemStack, ItemStack> consumer, Ingredient ingredient, Ingredient input) {
        RECIPES.computeIfAbsent(input, i -> new IdentityHashMap<>()).put(ingredient, consumer);
    }

    @Override
    public void updateResult() {
        ItemStack stack = getSlot(0).getStack();

        var lookup = RECIPES.entrySet().stream().filter(e -> e.getKey().test(stack))
                .flatMap(e -> e.getValue().entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
    protected boolean canUse(BlockState state) {
        return state.isOf(Blocks.FLETCHING_TABLE);
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.output && super.canInsertIntoSlot(stack, slot);
    }
}

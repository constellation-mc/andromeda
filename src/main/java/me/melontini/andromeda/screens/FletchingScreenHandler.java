package me.melontini.andromeda.screens;

import me.melontini.andromeda.registries.ScreenHandlerRegistry;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.minecraft.data.NbtBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public class FletchingScreenHandler extends ForgingScreenHandler {

    public FletchingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public FletchingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ScreenHandlerRegistry.FLETCHING_SCREEN_HANDLER, syncId, playerInventory, context);
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

    @Override
    public void updateResult() {
        ItemStack itemStack = getSlot(0).getStack();
        NbtCompound oldNbt = itemStack.getNbt();
        int i = 0;
        if (!(itemStack.getItem() instanceof BowItem) || !(getSlot(1).getStack().getItem() == Items.STRING)) {
            getSlot(2).setStack(ItemStack.EMPTY);
        } else {
            if (oldNbt != null) {
                i = oldNbt.getInt("AM-Tightened");
                oldNbt = oldNbt.copy();
            }
            if (i >= 32) return;
            ItemStack newStack = itemStack.copy();

            newStack.setNbt(NbtBuilder.create(oldNbt).putInt("AM-Tightened", Math.min(i + MathStuff.threadRandom().nextInt(1, 3), 32)).build());
            getSlot(2).setStack(newStack);
        }
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

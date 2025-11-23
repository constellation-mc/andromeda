package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.common.util.Keeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public class MerchantInventoryScreenHandler extends AbstractContainerMenu {

  public static final Keeper<MenuType<MerchantInventoryScreenHandler>> INSTANCE =
      Keeper.create();
  private final Container inventory;

  public MerchantInventoryScreenHandler(int syncId, Inventory playerInventory) {
    this(syncId, playerInventory, new SimpleContainer(8));
  }

  public MerchantInventoryScreenHandler(
          int syncId, Inventory playerInventory, Container inventory) {
    super(INSTANCE.orThrow(), syncId);
    this.inventory = inventory;
    inventory.startOpen(playerInventory.player);

    int i = -3 * 18;

    for (int k = 0; k < 8; ++k) {
      this.addSlot(new Slot(inventory, k, 17 + k * 18, 18));
    }

    for (int j = 0; j < 3; ++j) {
      for (int k = 0; k < 9; ++k) {
        this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 104 + j * 18 + i));
      }
    }

    for (int j = 0; j < 9; ++j) {
      this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 162 + i));
    }
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    ItemStack itemStack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);
    if (slot.hasItem()) {
      ItemStack itemStack2 = slot.getItem();
      itemStack = itemStack2.copy();
      if (index < 8) {
        if (!this.moveItemStackTo(itemStack2, 8, this.slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.moveItemStackTo(itemStack2, 0, 8, false)) {
        return ItemStack.EMPTY;
      }

      if (itemStack2.isEmpty()) {
        slot.setByPlayer(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
    }

    return itemStack;
  }

  @Override
  public boolean stillValid(Player player) {
    return this.inventory.stillValid(player);
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    this.inventory.stopOpen(player);
  }

  public Container getInventory() {
    return this.inventory;
  }
}

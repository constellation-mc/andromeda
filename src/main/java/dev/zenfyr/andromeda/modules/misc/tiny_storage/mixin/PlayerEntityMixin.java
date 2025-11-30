package dev.zenfyr.andromeda.modules.misc.tiny_storage.mixin;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.misc.tiny_storage.TinyStorage;
import dev.zenfyr.pulsar.nbt.NbtUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerEntityMixin {

  @Shadow
  @Final
  public InventoryMenu inventoryMenu;

  @Shadow
  @Nullable public abstract ItemEntity drop(ItemStack stack, boolean throwRandomly, boolean retainOwnership);

  @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
  private void andromeda$writeNbt(CompoundTag nbt, CallbackInfo ci) {
    NbtUtil.writeInventoryToTag("AM-Tiny-Storage", nbt, this.inventoryMenu.getCraftSlots());
  }

  @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
  private void andromeda$readNbt(CompoundTag nbt, CallbackInfo ci) {
    try {
      TinyStorage.LOADING.set(true); // We have to skip sending handler updates.
      NbtUtil.readInventoryFromTag("AM-Tiny-Storage", nbt, this.inventoryMenu.getCraftSlots());
    } finally {
      TinyStorage.LOADING.remove();
    }
  }

  @Inject(
      at =
          @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V"),
      method = "dropEquipment")
  private void andromeda$dropAll(CallbackInfo ci) {
    if (Andromeda.MAIN.get(TinyStorage.CONFIG).transferMode
        == TinyStorage.TransferMode.ALWAYS_TRANSFER) return;

    for (int i = 0; i < this.inventoryMenu.getCraftSlots().getContainerSize(); ++i) {
      ItemStack stack = this.inventoryMenu.getCraftSlots().removeItemNoUpdate(i);
      if (!stack.isEmpty() && EnchantmentHelper.hasVanishingCurse(stack)) continue;
      this.drop(stack, true, false);
    }
  }
}

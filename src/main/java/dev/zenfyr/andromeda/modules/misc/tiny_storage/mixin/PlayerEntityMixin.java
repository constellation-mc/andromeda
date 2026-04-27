package dev.zenfyr.andromeda.modules.misc.tiny_storage.mixin;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.misc.tiny_storage.TinyStorage;
import dev.zenfyr.pulsar.nbt.NbtUtil;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
  public abstract @Nullable ItemEntity drop(ItemStack itemStack, boolean bl);

  @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
  private void andromeda$writeNbt(ValueOutput valueOutput, CallbackInfo ci) {
    NbtUtil.writeInventoryToOutput(
        "AM-Tiny-Storage", valueOutput, this.inventoryMenu.getCraftSlots());
  }

  @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
  private void andromeda$readNbt(ValueInput valueInput, CallbackInfo ci) {
    try {
      TinyStorage.LOADING.set(true); // We have to skip sending handler updates.
      NbtUtil.readInventoryFromInput(
          "AM-Tiny-Storage", valueInput, this.inventoryMenu.getCraftSlots());
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
      if (!stack.isEmpty()
          && EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP))
        continue;
      this.drop(stack, true);
    }
  }
}

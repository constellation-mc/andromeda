package me.melontini.andromeda.modules.misc.tiny_storage.mixin;

import me.melontini.andromeda.modules.misc.tiny_storage.TinyStorage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
abstract class PlayerScreenHandlerMixin {

  @Redirect(
      at =
          @At(
              value = "INVOKE",
              target =
                      "Lnet/minecraft/world/inventory/InventoryMenu;clearContainer(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/Container;)V"),
      method = "removed")
  private void andromeda$doNotDrop(
          InventoryMenu instance, Player player, Container inventory) {}

  @Inject(at = @At("HEAD"), method = "slotsChanged", cancellable = true)
  private void andromeda$skipUpdate(Container inventory, CallbackInfo ci) {
    if (TinyStorage.LOADING.get()) ci.cancel();
  }
}

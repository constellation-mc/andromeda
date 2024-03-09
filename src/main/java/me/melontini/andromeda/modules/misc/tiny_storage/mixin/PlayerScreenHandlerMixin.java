package me.melontini.andromeda.modules.misc.tiny_storage.mixin;

import me.melontini.andromeda.modules.misc.tiny_storage.TinyStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
abstract class PlayerScreenHandlerMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;dropInventory(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/inventory/Inventory;)V"), method = "close")
    private void andromeda$doNotDrop(PlayerScreenHandler instance, PlayerEntity player, Inventory inventory) {

    }

    @Inject(at = @At("HEAD"), method = "onContentChanged", cancellable = true)
    private void andromeda$skipUpdate(Inventory inventory, CallbackInfo ci) {
        if (TinyStorage.LOADING.get()) ci.cancel();
    }
}

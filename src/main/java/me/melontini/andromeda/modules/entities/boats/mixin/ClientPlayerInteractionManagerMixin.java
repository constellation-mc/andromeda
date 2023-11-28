package me.melontini.andromeda.modules.entities.boats.mixin;

import me.melontini.andromeda.modules.entities.boats.entities.RideableInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
class ClientPlayerInteractionManagerMixin {//hasRidingInventory
    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("RETURN"), method = "hasRidingInventory", cancellable = true)
    private void hasRidingInventory(CallbackInfoReturnable<Boolean> cir) {
        boolean isRideable = this.client.player.hasVehicle() && this.client.player.getVehicle() instanceof RideableInventory;
        if (isRideable) {
            cir.setReturnValue(true);
        }
    }
}

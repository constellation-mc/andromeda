package me.melontini.andromeda.mixin.a_impl.rideable_inventory;

import me.melontini.andromeda.entity.vehicle.RideableInventory;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V", shift = At.Shift.AFTER), method = "onClientCommand", cancellable = true)
    private void onCommand(ClientCommandC2SPacket packet, CallbackInfo ci) {
        if (packet.getMode() == ClientCommandC2SPacket.Mode.OPEN_INVENTORY) {
            if (this.player.getVehicle() instanceof RideableInventory) {
                ((RideableInventory)this.player.getVehicle()).openInventory(this.player);
                ci.cancel();
            }
        }
    }
}
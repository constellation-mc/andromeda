package me.melontini.andromeda.common.mixin;

import me.melontini.andromeda.common.config.DataConfigs;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
abstract class ServerWorldMixin {

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerWorld;chunkManager:Lnet/minecraft/server/world/ServerChunkManager;", ordinal = 0, shift = At.Shift.AFTER), method = "<init>")
    private void andromeda$initStates(CallbackInfo ci) {
        DataConfigs.apply((ServerWorld) (Object) this);
    }
}

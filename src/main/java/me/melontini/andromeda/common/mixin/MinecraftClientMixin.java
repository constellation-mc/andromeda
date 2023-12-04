package me.melontini.andromeda.common.mixin;

import me.melontini.andromeda.common.client.AndromedaClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
class MinecraftClientMixin {

    @Inject(method = "method_29338", at = @At("TAIL"))
    private void andromeda$init(CallbackInfo ci) {
        AndromedaClient.get().lateInit();
    }
}

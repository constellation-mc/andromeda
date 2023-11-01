package me.melontini.andromeda.mixin.misc.unknown.useless_info;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.client.AndromedaClient;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(DebugHud.class)
@Feature("unknown")
class DebugHudMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;getServerWorldDebugString()Ljava/lang/String;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, method = "getLeftText")
    private void andromeda$leftText(CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
        if (Config.get().unknown) if (AndromedaClient.get().DEBUG_SPLASH != null) list.add(AndromedaClient.get().DEBUG_SPLASH);
    }
}

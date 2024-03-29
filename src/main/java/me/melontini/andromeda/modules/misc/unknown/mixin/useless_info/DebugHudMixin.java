package me.melontini.andromeda.modules.misc.unknown.mixin.useless_info;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.modules.misc.unknown.Main;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@SpecialEnvironment(Environment.CLIENT)
@Mixin(DebugHud.class)
abstract class DebugHudMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;getServerWorldDebugString()Ljava/lang/String;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, method = "getLeftText")
    private void andromeda$leftText(CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
        if (Main.DEBUG_SPLASH != null) list.add(Main.DEBUG_SPLASH);
    }
}

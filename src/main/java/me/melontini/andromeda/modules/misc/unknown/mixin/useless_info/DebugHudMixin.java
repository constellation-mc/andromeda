package me.melontini.andromeda.modules.misc.unknown.mixin.useless_info;

import com.google.common.base.Suppliers;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Supplier;

@SpecialEnvironment(Environment.CLIENT)
@Mixin(DebugHud.class)
abstract class DebugHudMixin {

    @Unique private static final Supplier<String> SPLASH = Suppliers.memoize(() -> {
        var r = MinecraftClient.getInstance().getSplashTextLoader().get();
        if (r != null) return r.text;
        return null;
    });

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;getServerWorldDebugString()Ljava/lang/String;", shift = At.Shift.BEFORE), method = "getLeftText")
    private void andromeda$leftText(CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
        if (SPLASH.get() != null) list.add(SPLASH.get());
    }
}

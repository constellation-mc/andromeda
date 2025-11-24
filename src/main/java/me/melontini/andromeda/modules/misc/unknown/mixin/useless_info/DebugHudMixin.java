package me.melontini.andromeda.modules.misc.unknown.mixin.useless_info;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import java.util.function.Supplier;
import me.melontini.andromeda.bootstrap.util.mixin.MixinEnvironment;
import me.melontini.dark_matter.api.base.util.functions.Memoize;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@MixinEnvironment(EnvType.CLIENT)
@Mixin(DebugScreenOverlay.class)
abstract class DebugHudMixin {

  @Unique private static final Supplier<String> SPLASH = Memoize.supplier(() -> {
    var r = Minecraft.getInstance().getSplashManager().getSplash();
    return r != null ? r.splash : null;
  });

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/components/DebugScreenOverlay;getServerChunkStats()Ljava/lang/String;",
              shift = At.Shift.BEFORE),
      method = "getGameInformation")
  private void andromeda$leftText(
      CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
    if (SPLASH.get() != null) list.add(SPLASH.get());
  }
}

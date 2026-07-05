package dev.zenfyr.andromeda.modules.misc.unknown.mixin.useless_info;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import dev.zenfyr.pulsar.api.platform.CEnvType;
import dev.zenfyr.pulsar.api.util.functions.Memoize;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinEnvironment(CEnvType.CLIENT)
@Mixin(DebugScreenOverlay.class)
abstract class DebugScreenOverlayMixin {

  @Shadow
  @Final
  private Minecraft minecraft;

  @Unique private static final Supplier<Component> SPLASH = Memoize.supplier(() -> {
    var r = Minecraft.getInstance().gui.splashManager().getSplash();
    return r != null ? r.splash : null;
  });

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Ljava/util/List;isEmpty()Z",
              ordinal = 0,
              shift = At.Shift.BEFORE),
      method = "extractRenderState")
  private void andromeda$leftText(
      GuiGraphicsExtractor guiGraphics, CallbackInfo ci, @Local(index = 6) List<String> list) {
    if (this.minecraft.debugEntries.isOverlayVisible() && SPLASH.get() != null)
      list.add(SPLASH.get().getString());
  }
}

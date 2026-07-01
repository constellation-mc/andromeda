package dev.zenfyr.andromeda.modules.misc.unknown.mixin.nice_level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import dev.zenfyr.pulsar.api.platform.CEnvType;
import java.util.Objects;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment(CEnvType.CLIENT)
@Mixin(LevelLoadingScreen.class)
abstract class LevelLoadingScreenMixin {

  @ModifyReturnValue(at = @At("RETURN"), method = "getFormattedProgress")
  private String andromeda$getPercentage(String o) {
    if (Objects.equals(o, "69%")) {
      return "Nice%";
    }
    return o;
  }
}

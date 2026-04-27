package dev.zenfyr.andromeda.modules.misc.unknown.mixin.nice_level;

import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;

@MixinEnvironment(EnvType.CLIENT)
@Mixin(LevelLoadingScreen.class)
abstract class LevelLoadingScreenMixin {

  // TODO: find a new injection point
  //  @ModifyReturnValue(at = @At("RETURN"), method = "getFormattedProgress")
  //  private String andromeda$getPercentage(String o) {
  //    if (Objects.equals(o, "69%")) {
  //      return "Nice%";
  //    }
  //    return o;
  //  }
}

package me.melontini.andromeda.modules.misc.unknown.mixin.nice_level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.Objects;
import me.melontini.andromeda.bootstrap.util.mixin.MixinEnvironment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment(EnvType.CLIENT)
@Mixin(LevelLoadingScreen.class)
abstract class LevelLoadingScreenMixin {

  @ModifyReturnValue(at = @At("RETURN"), method = "getPercentage")
  private String andromeda$getPercentage(String o) {
    if (Objects.equals(o, "69%")) {
      return "Nice%";
    }
    return o;
  }
}

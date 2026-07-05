package dev.zenfyr.andromeda.modules.gui.no_more_adventure.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameModeSwitcherScreen.GameModeIcon.class)
abstract class GameModeIconMixin {
  @ModifyExpressionValue(
      method = "getNext",
      at =
          @At(
              value = "FIELD",
              target =
                  "Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen$GameModeIcon;ADVENTURE:Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen$GameModeIcon;",
              opcode = Opcodes.GETSTATIC))
  private GameModeSwitcherScreen.GameModeIcon andromeda$next(
      GameModeSwitcherScreen.GameModeIcon original) {
    return GameModeSwitcherScreen.GameModeIcon.SPECTATOR;
  }
}

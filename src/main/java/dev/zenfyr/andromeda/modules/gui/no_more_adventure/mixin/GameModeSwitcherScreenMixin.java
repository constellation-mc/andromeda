package dev.zenfyr.andromeda.modules.gui.no_more_adventure.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameModeSwitcherScreen.class)
abstract class GameModeSwitcherScreenMixin extends Screen {
  protected GameModeSwitcherScreenMixin(Component title) {
    super(title);
  }

  @Unique private final GameModeSwitcherScreen.GameModeIcon[] andromeda$gameModeSelections =
      ArrayUtils.removeElement(
          GameModeSwitcherScreen.GameModeIcon.values(),
          GameModeSwitcherScreen.GameModeIcon.ADVENTURE);

  @ModifyExpressionValue(
      method = "init",
      at =
          @At(
              value = "FIELD",
              target =
                  "Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen$GameModeIcon;VALUES:[Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen$GameModeIcon;",
              opcode = Opcodes.GETSTATIC))
  private GameModeSwitcherScreen.GameModeIcon[] andromeda$modValues(
      GameModeSwitcherScreen.GameModeIcon[] original) {
    return andromeda$gameModeSelections;
  }

  @ModifyExpressionValue(
      method = "init",
      at =
          @At(
              value = "FIELD",
              target =
                  "Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen;ALL_SLOTS_WIDTH:I",
              opcode = Opcodes.GETSTATIC))
  private int andromeda$modValues(int original) {
    return andromeda$gameModeSelections.length * 31 - 5;
  }
}

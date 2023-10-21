package me.melontini.andromeda.mixin.gui.bye_adventure;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameModeSelectionScreen.GameModeSelection.class)
@Feature("noMoreAdventure")
class GameModeSelectionMixin {
    @ModifyExpressionValue(method = "next", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;ADVENTURE:Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;"))
    private GameModeSelectionScreen.GameModeSelection andromeda$next(GameModeSelectionScreen.GameModeSelection original) {
        return !Config.get().noMoreAdventure ? original : GameModeSelectionScreen.GameModeSelection.SPECTATOR;
    }
}

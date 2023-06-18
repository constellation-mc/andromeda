package me.melontini.andromeda.mixin.gui.bye_adventure;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameModeSelectionScreen.GameModeSelection.class)
@MixinRelatedConfigOption("noMoreAdventure")
public class GameModeSelectionMixin {
    @ModifyExpressionValue(method = "next", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;ADVENTURE:Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;"))
    private GameModeSelectionScreen.GameModeSelection andromeda$next(GameModeSelectionScreen.GameModeSelection original) {
        return !Andromeda.CONFIG.noMoreAdventure ? original : GameModeSelectionScreen.GameModeSelection.SPECTATOR;
    }
}

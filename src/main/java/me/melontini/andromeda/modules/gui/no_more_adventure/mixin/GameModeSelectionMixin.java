package me.melontini.andromeda.modules.gui.no_more_adventure.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.gui.no_more_adventure.NoMoreAdventure;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameModeSelectionScreen.GameModeSelection.class)
class GameModeSelectionMixin {
    @Unique
    private static final NoMoreAdventure am$noma = ModuleManager.quick(NoMoreAdventure.class);
    @ModifyExpressionValue(method = "next", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;ADVENTURE:Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;"))
    private GameModeSelectionScreen.GameModeSelection andromeda$next(GameModeSelectionScreen.GameModeSelection original) {
        return !am$noma.config().enabled ? original : GameModeSelectionScreen.GameModeSelection.SPECTATOR;
    }
}

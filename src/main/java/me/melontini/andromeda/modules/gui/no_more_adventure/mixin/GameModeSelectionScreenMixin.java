package me.melontini.andromeda.modules.gui.no_more_adventure.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.gui.no_more_adventure.NoMoreAdventure;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameModeSelectionScreen.class)
abstract class GameModeSelectionScreenMixin extends Screen {
    @Unique
    private static final NoMoreAdventure am$noma = ModuleManager.quick(NoMoreAdventure.class);
    protected GameModeSelectionScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private final GameModeSelectionScreen.GameModeSelection[] andromeda$gameModeSelections = ArrayUtils.removeElement(GameModeSelectionScreen.GameModeSelection.values(), GameModeSelectionScreen.GameModeSelection.ADVENTURE);

    @ModifyExpressionValue(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;VALUES:[Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;"))
    private GameModeSelectionScreen.GameModeSelection[] andromeda$modValues(GameModeSelectionScreen.GameModeSelection[] original) {
        return !am$noma.config().enabled ? original : andromeda$gameModeSelections;
    }

    @ModifyExpressionValue(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/GameModeSelectionScreen;UI_WIDTH:I"))
    private int andromeda$modValues(int original) {
        return !am$noma.config().enabled ? original : andromeda$gameModeSelections.length * 31 - 5;
    }
}

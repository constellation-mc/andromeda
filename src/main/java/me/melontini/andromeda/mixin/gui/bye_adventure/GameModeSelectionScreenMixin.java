package me.melontini.andromeda.mixin.gui.bye_adventure;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameModeSelectionScreen.class)
@MixinRelatedConfigOption("noMoreAdventure")
public abstract class GameModeSelectionScreenMixin extends Screen {
    protected GameModeSelectionScreenMixin(Text title) {
        super(title);
    }

    private final GameModeSelectionScreen.GameModeSelection[] andromeda$gameModeSelections = ArrayUtils.removeElement(GameModeSelectionScreen.GameModeSelection.values(), GameModeSelectionScreen.GameModeSelection.ADVENTURE);

    @ModifyExpressionValue(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;VALUES:[Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;"))
    private GameModeSelectionScreen.GameModeSelection[] andromeda$modValues(GameModeSelectionScreen.GameModeSelection[] original) {
        return !Andromeda.CONFIG.noMoreAdventure ? original : andromeda$gameModeSelections;
    }

    @ModifyExpressionValue(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/GameModeSelectionScreen;UI_WIDTH:I"))
    private int andromeda$modValues(int original) {
        return !Andromeda.CONFIG.noMoreAdventure ? original : andromeda$gameModeSelections.length * 31 - 5;
    }
}

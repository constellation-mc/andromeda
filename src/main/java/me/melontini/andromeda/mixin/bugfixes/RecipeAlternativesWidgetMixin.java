package me.melontini.andromeda.mixin.bugfixes;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.client.gui.screen.recipebook.RecipeAlternativesWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RecipeAlternativesWidget.class)
@Feature("properlyAlignedRecipeAlternatives")
class RecipeAlternativesWidgetMixin {
    @Shadow private int buttonX;
    @Shadow private int buttonY;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeAlternativesWidget;renderGrid(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"), index = 3, method = "render")
    private int andromeda$prepareGrid(int i) {
        return Config.get().properlyAlignedRecipeAlternatives ? 25 : i;
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeAlternativesWidget;buttonX:I"), method = "renderGrid")
    private int andromeda$renderGridX(RecipeAlternativesWidget instance) {
        return Config.get().properlyAlignedRecipeAlternatives ? this.buttonX - 2 : this.buttonX;
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeAlternativesWidget;buttonY:I"), method = "renderGrid")
    private int andromeda$renderGridY(RecipeAlternativesWidget instance) {
        return Config.get().properlyAlignedRecipeAlternatives ? this.buttonY - 1 : this.buttonY;
    }
}

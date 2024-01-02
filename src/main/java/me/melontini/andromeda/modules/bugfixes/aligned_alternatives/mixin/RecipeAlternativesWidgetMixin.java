package me.melontini.andromeda.modules.bugfixes.aligned_alternatives.mixin;

import net.minecraft.client.gui.screen.recipebook.RecipeAlternativesWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RecipeAlternativesWidget.class)
abstract class RecipeAlternativesWidgetMixin {

    @Shadow private int buttonX;
    @Shadow private int buttonY;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeAlternativesWidget;renderGrid(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"), index = 3, method = "render")
    private int andromeda$prepareGrid(int i) {
        return i;
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeAlternativesWidget;buttonX:I"), method = "renderGrid")
    private int andromeda$renderGridX(RecipeAlternativesWidget instance) {
        return this.buttonX - 2;
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeAlternativesWidget;buttonY:I"), method = "renderGrid")
    private int andromeda$renderGridY(RecipeAlternativesWidget instance) {
        return this.buttonY - 1;
    }
}

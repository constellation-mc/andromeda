package me.melontini.andromeda.mixin.bugfixes.aligned_alternatives;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.bugfixes.aligned_alternatives.AlignedRecipeAlternatives;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.client.gui.screen.recipebook.RecipeAlternativesWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RecipeAlternativesWidget.class)
@Feature("properlyAlignedRecipeAlternatives")
class RecipeAlternativesWidgetMixin {
    @Unique
    private static final AlignedRecipeAlternatives am$para = ModuleManager.quick(AlignedRecipeAlternatives.class);

    @Shadow private int buttonX;
    @Shadow private int buttonY;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeAlternativesWidget;renderGrid(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"), index = 3, method = "render")
    private int andromeda$prepareGrid(int i) {
        return am$para.config().enabled ? 25 : i;
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeAlternativesWidget;buttonX:I"), method = "renderGrid")
    private int andromeda$renderGridX(RecipeAlternativesWidget instance) {
        return am$para.config().enabled ? this.buttonX - 2 : this.buttonX;
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeAlternativesWidget;buttonY:I"), method = "renderGrid")
    private int andromeda$renderGridY(RecipeAlternativesWidget instance) {
        return am$para.config().enabled ? this.buttonY - 1 : this.buttonY;
    }
}

package me.melontini.andromeda.modules.misc.recipe_advancements_generation.mixin;

import me.melontini.andromeda.modules.misc.recipe_advancements_generation.ItemPredicateAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPredicate.class)
abstract class ItemPredicateMixin implements ItemPredicateAccessor {

    @Unique
    private Ingredient andromeda$ingredient;//Really stretching records over here.

    @Inject(at = @At("HEAD"), method = "test", cancellable = true)
    private void andromeda$test(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.andromeda$ingredient != null) {
            cir.setReturnValue(this.andromeda$ingredient.test(stack));
        }
    }

    @Override
    public void andromeda$setIngredient(Ingredient ingredient) {
        this.andromeda$ingredient = ingredient;
    }
}

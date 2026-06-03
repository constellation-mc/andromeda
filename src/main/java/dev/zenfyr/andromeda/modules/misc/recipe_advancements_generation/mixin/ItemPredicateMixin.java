package dev.zenfyr.andromeda.modules.misc.recipe_advancements_generation.mixin;

import dev.zenfyr.andromeda.modules.misc.recipe_advancements_generation.ItemPredicateAccessor;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPredicate.class)
abstract class ItemPredicateMixin implements ItemPredicateAccessor {

  @Unique private Ingredient andromeda$ingredient;

  @Inject(
      at = @At("HEAD"),
      method = "test(Lnet/minecraft/world/item/ItemStack;)Z",
      cancellable = true)
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

package dev.zenfyr.andromeda.modules.misc.recipe_advancements_generation.mixin;

import dev.zenfyr.andromeda.modules.misc.recipe_advancements_generation.ItemPredicateAccessor;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.world.item.ItemInstance;
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
      method = "test(Lnet/minecraft/world/item/ItemInstance;)Z",
      cancellable = true)
  private void andromeda$test(ItemInstance itemStack, CallbackInfoReturnable<Boolean> cir) {
    if (this.andromeda$ingredient != null) {
      cir.setReturnValue(itemStack.is(
          ((IngredientAccessor) (Object) this.andromeda$ingredient).andromeda$values()));
    }
  }

  @Override
  public void andromeda$setIngredient(Ingredient ingredient) {
    this.andromeda$ingredient = ingredient;
  }
}

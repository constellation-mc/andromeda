package me.melontini.andromeda.api;

import java.util.function.BiPredicate;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

public class ModuleDeclarations {

  /**
   * Provide filters to exclude recipes from automatic advancement generation.
   */
  public static final ApiDeclaration<RecipeFilter, Void> ADVANCEMENT_RECIPE_FILTER =
      new ApiDeclaration<>(ApiDeclaration.Status.STABLE);

  public interface RecipeFilter extends BiPredicate<Identifier, Recipe<?>> {
    @Override
    boolean test(Identifier identifier, Recipe<?> recipe);
  }

  public static final ApiDeclaration<LootUnlocker, Void> LOOT_UNLOCKER =
      new ApiDeclaration<>(ApiDeclaration.Status.STABLE);

  public interface LootUnlocker extends BiPredicate<BlockEntity, PlayerEntity> {
    @Override
    boolean test(BlockEntity be, PlayerEntity player);
  }
}

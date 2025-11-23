package me.melontini.andromeda.api;

import java.util.function.BiPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ModuleDeclarations {

  /**
   * Provide filters to exclude recipes from automatic advancement generation.
   */
  public static final ApiDeclaration<RecipeFilter, Void> ADVANCEMENT_RECIPE_FILTER =
      new ApiDeclaration<>(ApiDeclaration.Status.STABLE);

  public interface RecipeFilter extends BiPredicate<ResourceLocation, Recipe<?>> {
    @Override
    boolean test(ResourceLocation identifier, Recipe<?> recipe);
  }

  public static final ApiDeclaration<LootUnlocker, Void> LOOT_UNLOCKER =
      new ApiDeclaration<>(ApiDeclaration.Status.STABLE);

  public interface LootUnlocker extends BiPredicate<BlockEntity, Player> {
    @Override
    boolean test(BlockEntity be, Player player);
  }
}

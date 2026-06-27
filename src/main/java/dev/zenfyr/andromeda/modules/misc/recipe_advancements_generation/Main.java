package dev.zenfyr.andromeda.modules.misc.recipe_advancements_generation;

import com.google.common.collect.ImmutableMap;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.pulsar.api.util.MakeSure;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.advancements.*;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

public final class Main {
  private static final Map<RecipeType<?>, Function<Context, Return>> RECIPE_TYPE_HANDLERS =
      new HashMap<>();
  private static final List<BiPredicate<Identifier, Recipe<?>>> FILTERS =
      Collections.synchronizedList(new ArrayList<>());

  public static Function<Context, Return> basicConsumer(
      String typeName, AdvancementGeneration.Config config) {
    return context -> {
      if (!(context.recipe() instanceof SingleItemRecipe sir)) {
        ModuleManager.get()
            .get(AdvancementGeneration.class)
            .orElseThrow()
            .logger()
            .error("Single item factory requested for non single item recipe! {}", context.key());
        return null;
      }
      return new Return(
          idFromRecipe(context.key(), typeName),
          createAdvBuilder(config, context.key(), sir.input()));
    };
  }

  private static ResourceKey<Recipe<?>> idFromRecipe(
      ResourceKey<Recipe<?>> recipe, String typeName) {
    return ResourceKey.create(
        Registries.RECIPE,
        Identifier.fromNamespaceAndPath(
            recipe.identifier().getNamespace(),
            "recipes/gen/" + typeName + "/" + recipe.identifier().toString().replace(":", "_")));
  }

  public static void addRecipeTypeHandler(RecipeType<?> type, Function<Context, Return> consumer) {
    RECIPE_TYPE_HANDLERS.putIfAbsent(type, consumer);
  }

  public static void generateRecipeAdvancements(
      MinecraftServer server, AdvancementGeneration module, AdvancementGeneration.Config config) {
    Map<Identifier, AdvancementHolder> advancementBuilders = new ConcurrentHashMap<>();

    List<CompletableFuture<Void>> futures = server.getRecipeManager().getRecipes().stream()
        .filter(recipe -> {
          for (BiPredicate<Identifier, Recipe<?>> filter : FILTERS) {
            if (filter.test(recipe.id().identifier(), recipe.value())) return false;
          }
          return true;
        })
        .map(recipe -> CompletableFuture.runAsync(
            () -> {
              var handler = RECIPE_TYPE_HANDLERS.get(recipe.value().getType());
              if (handler != null) {
                var r = handler.apply(new Context(recipe.value(), recipe.id()));
                if (r != null)
                  advancementBuilders.put(
                      r.key().identifier(), r.builder().build(r.key().identifier()));
              }
            },
            Util.backgroundExecutor()))
        .toList();
    // and?
    CompletableFuture<Void> future =
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    server.managedBlock(future::isDone);
    module.logger().info("finished generating {} recipe advancements", advancementBuilders.size());

    advancementBuilders.putAll(server.getAdvancements().advancements);
    server.getAdvancements().advancements = ImmutableMap.copyOf(advancementBuilders);
    AdvancementTree advancementManager = server.getAdvancements().tree();
    advancementManager.addAll(advancementBuilders.values());

    advancementBuilders.clear();
  }

  public static @NotNull Advancement.Builder createAdvBuilder(
      AdvancementGeneration.Config config, ResourceKey<Recipe<?>> id, Ingredient... ingredients) {
    MakeSure.notEmpty(ingredients); // shouldn't really happen
    var builder = Advancement.Builder.recipeAdvancement();
    builder.parent(Identifier.tryBuild("minecraft", "recipes/root"));

    List<String> names = new ArrayList<>();
    Set<Ingredient> elements = new HashSet<>();
    for (int i = 0; i < ingredients.length; i++) {
      var ingredient = ingredients[i];

      if (ingredient.isEmpty()) continue;
      if (!elements.add(ingredient)) continue;

      var name = String.valueOf(i);
      names.add(name);
      var predicate = ItemPredicate.Builder.item().build();
      ((ItemPredicateAccessor) (Object) predicate).andromeda$setIngredient(ingredient);
      builder.addCriterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(predicate));
    }
    builder.addCriterion("has_recipe", RecipeUnlockedTrigger.unlocked(id));

    String[][] reqs;
    if (config.requireAllItems) {
      reqs = new String[names.size()][2];
      for (int i = 0; i < names.size(); i++) {
        String s = names.get(i);
        reqs[i][0] = s;
        reqs[i][1] = "has_recipe";
      }
    } else {
      reqs = new String[1][names.size() + 1];
      for (int i = 0; i < names.size(); i++) {
        String s = names.get(i);
        reqs[0][i] = s;
      }
      reqs[0][names.size()] = "has_recipe";
    }
    builder.requirements(
        new AdvancementRequirements(Arrays.stream(reqs).map(List::of).toList()));

    builder.rewards(AdvancementRewards.Builder.recipe(id).build());
    return builder;
  }

  static void init(AdvancementGeneration module, AdvancementGeneration.Config config) {
    FILTERS.add((id, recipe) -> config.namespaceBlacklist.contains(id.getNamespace()));
    FILTERS.add((id, recipe) -> config.recipeBlacklist.contains(id));
    FILTERS.add((id, recipe) -> recipe.isSpecial() && config.ignoreRecipesHiddenInTheRecipeBook);

    ServerLifecycleEvents.SERVER_STARTING.register(
        server -> generateRecipeAdvancements(server, module, config));
    BeforeDataPackSyncEvent.EVENT.register(
        server -> generateRecipeAdvancements(server, module, config));

    addRecipeTypeHandler(RecipeType.BLASTING, basicConsumer("blasting", config));
    addRecipeTypeHandler(RecipeType.SMOKING, basicConsumer("smoking", config));
    addRecipeTypeHandler(RecipeType.SMELTING, basicConsumer("smelting", config));
    addRecipeTypeHandler(RecipeType.CAMPFIRE_COOKING, basicConsumer("campfire_cooking", config));
    addRecipeTypeHandler(RecipeType.STONECUTTING, basicConsumer("stonecutting", config));
    addRecipeTypeHandler(RecipeType.SMITHING, context -> {
      if (!(context.recipe() instanceof SmithingRecipe sr)) {
        module
            .logger()
            .error(
                "Smithing recipe factory requested for non smithing recipe type! {}",
                context.key());
        return null;
      }

      return new Return(
          idFromRecipe(context.key(), "crafting"),
          createAdvBuilder(
              config,
              context.key(),
              sr.baseIngredient(),
              sr.additionIngredient().orElse(Ingredient.of())));
    });
    addRecipeTypeHandler(RecipeType.CRAFTING, (context) -> {
      if (!(context.recipe() instanceof CraftingRecipe)) {
        module
            .logger()
            .error(
                "Crafting recipe factory requested for non crafting recipe type! {}",
                context.key());
        return null;
      }
      if (context.recipe() instanceof CustomRecipe) return null;

      if (context.recipe() instanceof ShapelessRecipe recipe) {
        if (recipe.ingredients.isEmpty()) return null;

        return new Return(
            idFromRecipe(context.key(), "crafting"),
            createAdvBuilder(config, context.key(), recipe.ingredients.toArray(Ingredient[]::new)));
      }

      if (context.recipe() instanceof ShapedRecipe recipe) {
        if (recipe.pattern.ingredients().isEmpty()) return null;

        return new Return(
            idFromRecipe(context.key(), "crafting"),
            createAdvBuilder(
                config,
                context.key(),
                recipe.pattern.ingredients().stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toArray(Ingredient[]::new)));
      }

      if (context.recipe() instanceof TransmuteRecipe recipe) {
        if (recipe.input.isEmpty() && recipe.material.isEmpty()) return null;

        return new Return(
            idFromRecipe(context.key(), "crafting"),
            createAdvBuilder(config, context.key(), recipe.input, recipe.material));
      }

      return null;
    });
  }

  public record Return(ResourceKey<Recipe<?>> key, Advancement.Builder builder) {}

  public record Context(Recipe<?> recipe, ResourceKey<Recipe<?>> key) {}
}

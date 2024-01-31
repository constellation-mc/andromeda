package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementPositioner;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Main {
    private static final Keeper<AdvancementGeneration> MODULE = Keeper.create();
    private static final Map<RecipeType<?>, Function<Context, Return>> RECIPE_TYPE_HANDLERS = new HashMap<>();

    public static Function<Context, Return> basicConsumer(String typeName) {
        return context -> new Return(idFromRecipe(context.id(), typeName), createAdvBuilder(context.id(), context.recipe().getIngredients().get(0)));
    }

    private static Identifier idFromRecipe(Identifier recipe, String typeName) {
        return new Identifier(recipe.getNamespace(), "recipes/gen/" + typeName + "/" + recipe.toString().replace(":", "_"));
    }

    public static void addRecipeTypeHandler(RecipeType<?> type, Function<Context, Return> consumer) {
        RECIPE_TYPE_HANDLERS.putIfAbsent(type, consumer);
    }

    public static void generateRecipeAdvancements(MinecraftServer server) {
        AdvancementGeneration module = MODULE.orThrow();
        Map<Identifier, Advancement.Builder> advancementBuilders = new ConcurrentHashMap<>();
        AtomicInteger count = new AtomicInteger();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<List<Recipe<?>>> lists = Lists.partition(server.getRecipeManager().values().stream().toList(), 800);
        for (List<Recipe<?>> list : lists) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (Recipe<?> recipe : list) {
                    if (module.config().namespaceBlacklist.contains(recipe.getId().getNamespace()))
                        continue;
                    if (module.config().recipeBlacklist.contains(recipe.getId().toString()))
                        continue;
                    if (recipe.isIgnoredInRecipeBook() && module.config().ignoreRecipesHiddenInTheRecipeBook)
                        continue;

                    var handler = RECIPE_TYPE_HANDLERS.get(recipe.getType());
                    if (handler != null) {
                        count.getAndIncrement();
                        var r = handler.apply(new Context(recipe, recipe.getId()));
                        if (r != null) advancementBuilders.put(r.id(), r.builder());
                    } else {
                        if (!recipe.getIngredients().isEmpty()) {
                            count.getAndIncrement();
                            advancementBuilders.put(new Identifier(recipe.getId().getNamespace(), "recipes/gen/generic/" + recipe.getId().toString().replace(":", "_")), createAdvBuilder(recipe.getId(), recipe.getIngredients().toArray(Ingredient[]::new)));
                        }
                    }
                }
            }, Util.getMainWorkerExecutor()));
        }

        //and?
        CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));

        server.runTasks(future::isDone);

        AdvancementManager advancementManager = server.getAdvancementLoader().manager;
        advancementManager.load(advancementBuilders);

        for (Advancement advancement : advancementManager.getRoots()) {
            if (advancement.getDisplay() != null) {
                AdvancementPositioner.arrangeForTree(advancement);
            }
        }

        AndromedaLog.info("finished generating {} recipe advancements", count.get());
        advancementBuilders.clear();
    }

    static final class CustomPredicate extends ItemPredicate {
        private final Ingredient ingredient;

        CustomPredicate(Ingredient ingredient) {
            this.ingredient = ingredient;
        }

        @Override
        public boolean test(ItemStack stack) {
            return ingredient.test(stack);
        }

        @Override
        public JsonElement toJson() {
            return ANY.toJson();
        }
    }

    public static @NotNull Advancement.Builder createAdvBuilder(Identifier id, Ingredient... ingredients) {
        MakeSure.notEmpty(ingredients);// shouldn't really happen
        var builder = Advancement.Builder.createUntelemetered();
        builder.parent(Identifier.tryParse("minecraft:recipes/root"));

        List<String> names = new ArrayList<>();
        Set<JsonElement> elements = new HashSet<>();
        for (int i = 0; i < ingredients.length; i++) {
            var ingredient = ingredients[i];

            if (ingredient.isEmpty()) continue;
            if (!elements.add(ingredient.toJson())) continue;

            var name = String.valueOf(i);
            names.add(name);
            builder.criterion(name, InventoryChangedCriterion.Conditions.items(new CustomPredicate(ingredient)));
        }
        builder.criterion("has_recipe", new RecipeUnlockedCriterion.Conditions(EntityPredicate.Extended.create(), id));

        String[][] reqs;
        if (MODULE.orThrow().config().requireAllItems) {
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
        builder.requirements(reqs);

        Optional.ofNullable(AdvancementRewards.Builder.recipe(id).build()).ifPresent(builder::rewards);
        return builder;
    }

    Main(AdvancementGeneration module) {
        Main.MODULE.init(module);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Main.generateRecipeAdvancements(server);
            server.getPlayerManager().getPlayerList().forEach(entity -> server.getPlayerManager().getAdvancementTracker(entity).reload(server.getAdvancementLoader()));
        });

        addRecipeTypeHandler(RecipeType.BLASTING, basicConsumer("blasting"));
        addRecipeTypeHandler(RecipeType.SMOKING, basicConsumer("smoking"));
        addRecipeTypeHandler(RecipeType.SMELTING, basicConsumer("smelting"));
        addRecipeTypeHandler(RecipeType.CAMPFIRE_COOKING, basicConsumer("campfire_cooking"));
        addRecipeTypeHandler(RecipeType.STONECUTTING, basicConsumer("stonecutting"));
        addRecipeTypeHandler(RecipeType.CRAFTING, (context) -> {
            if (!(context.recipe() instanceof SpecialCraftingRecipe)) {
                if (!context.recipe().getIngredients().isEmpty()) {
                    return new Return(idFromRecipe(context.id(), "crafting"), createAdvBuilder(context.id(), context.recipe().getIngredients().toArray(Ingredient[]::new)));
                }
            }
            return null;
        });
    }

    public record Return(Identifier id, Advancement.Builder builder) {
    }

    public record Context(Recipe<?> recipe, Identifier id) {
    }
}

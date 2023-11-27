package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.classes.Lazy;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementPositioner;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class Helper {
    private static final Lazy<AdvancementGeneration> module = Lazy.of(() -> () -> ModuleManager.quick(AdvancementGeneration.class));

    public static final Hash.Strategy<ItemStack> STRATEGY = new Hash.Strategy<>() {//Vanilla ItemStackSet is good, but it uses canCombine() and not areEqual(). It also doesn't exist on 1.18.2-1.19.2
        @Override
        public int hashCode(ItemStack itemStack) {
            if (itemStack != null) {
                NbtCompound nbtCompound = itemStack.getNbt();
                int i = 31 + itemStack.getItem().hashCode();
                return 31 * i + (nbtCompound == null ? 0 : nbtCompound.hashCode());
            } else {
                return 0;
            }
        }

        @Override
        public boolean equals(ItemStack itemStack, ItemStack itemStack2) {
            return itemStack == itemStack2
                    || itemStack != null && itemStack2 != null && itemStack.isEmpty() == itemStack2.isEmpty() && ItemStack.areEqual(itemStack, itemStack2);
        }
    };
    private static final Map<RecipeType<?>, BiConsumer<Map<Identifier, Advancement.Builder>, Recipe<?>>> RECIPE_TYPE_HANDLERS = new HashMap<>();

    static {
        addRecipeTypeHandler(RecipeType.BLASTING, basicConsumer("blasting"));
        addRecipeTypeHandler(RecipeType.SMOKING, basicConsumer("smoking"));
        addRecipeTypeHandler(RecipeType.SMELTING, basicConsumer("smelting"));
        addRecipeTypeHandler(RecipeType.CAMPFIRE_COOKING, basicConsumer("campfire_cooking"));
        addRecipeTypeHandler(RecipeType.STONECUTTING, basicConsumer("stonecutting"));
        addRecipeTypeHandler(RecipeType.CRAFTING, (map, recipe) -> {
            if (!(recipe instanceof SpecialCraftingRecipe)) {
                if (!recipe.getIngredients().isEmpty()) {
                    map.put(idFromRecipe(recipe, "crafting"), createAdvBuilder(recipe.getId(), recipe.getIngredients().toArray(Ingredient[]::new)));
                }
            }
        });
    }

    public static BiConsumer<Map<Identifier, Advancement.Builder>, Recipe<?>> basicConsumer(String typeName) {
        return (map, recipe) -> map.put(idFromRecipe(recipe, typeName), createAdvBuilder(recipe.getId(), recipe.getIngredients().get(0)));
    }

    private static Identifier idFromRecipe(Recipe<?> recipe, String typeName) {
        return new Identifier(recipe.getId().getNamespace(), "recipes/gen/" + typeName + "/" + recipe.getId().toString().replace(":", "_"));
    }

    public static void addRecipeTypeHandler(RecipeType<?> type, BiConsumer<Map<Identifier, Advancement.Builder>, Recipe<?>> consumer) {
        RECIPE_TYPE_HANDLERS.putIfAbsent(type, consumer);
    }

    public static void generateRecipeAdvancements(MinecraftServer server) {
        Map<Identifier, Advancement.Builder> advancementBuilders = new ConcurrentHashMap<>();
        AtomicInteger count = new AtomicInteger();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<List<Recipe<?>>> lists = Lists.partition(server.getRecipeManager().values().stream().toList(), 800);
        for (List<Recipe<?>> list : lists) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (Recipe<?> recipe : list) {
                    if (module.get().config().namespaceBlacklist.contains(recipe.getId().getNamespace()))
                        continue;
                    if (module.get().config().recipeBlacklist.contains(recipe.getId().toString()))
                        continue;
                    if (recipe.isIgnoredInRecipeBook() && module.get().config().ignoreRecipesHiddenInTheRecipeBook)
                        continue;

                    if (RECIPE_TYPE_HANDLERS.get(recipe.getType()) != null) {
                        count.getAndIncrement();
                        RECIPE_TYPE_HANDLERS.get(recipe.getType()).accept(advancementBuilders, recipe);
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

    public static @NotNull Advancement.Builder createAdvBuilder(Identifier id, Ingredient... ingredients) {
        MakeSure.notEmpty(ingredients);// shouldn't really happen
        var builder = Advancement.Builder.create();
        builder.parent(Identifier.tryParse("minecraft:recipes/root"));

        List<String> names = new ArrayList<>();
        Set<TagKey<Item>> tags = new HashSet<>();
        Set<ItemStack> stacks = new ObjectLinkedOpenCustomHashSet<>(STRATEGY);

        for (int i = 0; i < ingredients.length; i++) {
            Ingredient ingredient = ingredients[i];
            List<ItemPredicate> predicates = new ArrayList<>();
            for (int j = 0; j < ingredient.entries.length; j++) {
                Ingredient.Entry entry = ingredient.entries[j];
                if (entry instanceof Ingredient.StackEntry stackEntry) {
                    if (!stackEntry.stack.isEmpty()) {
                        if (stacks.contains(stackEntry.stack)) continue;
                        stacks.add(stackEntry.stack);
                        names.add(String.valueOf(i));
                        ItemPredicate.Builder predicateBuilder = ItemPredicate.Builder.create()
                                .items(stackEntry.stack.getItem());
                        if (stackEntry.stack.getNbt() != null) predicateBuilder.nbt(stackEntry.stack.getNbt());
                        Optional.ofNullable(predicateBuilder.build()).ifPresent(predicates::add);
                    }
                } else if (entry instanceof Ingredient.TagEntry tagEntry) {
                    if (tags.contains(tagEntry.tag)) continue;
                    tags.add(tagEntry.tag);
                    names.add(String.valueOf(i));
                    Optional.ofNullable(ItemPredicate.Builder.create().tag(tagEntry.tag).build())
                            .ifPresent(predicates::add);
                } else {
                    AndromedaLog.error("unknown ingredient found in {}", id);
                }
            }
            builder.criterion(String.valueOf(i), InventoryChangedCriterion.Conditions.items(predicates.toArray(ItemPredicate[]::new)));
        }
        builder.criterion("has_recipe", new RecipeUnlockedCriterion.Conditions(LootContextPredicate.create(), id));

        String[][] reqs;
        if (module.get().config().requireAllItems) {
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
        names.clear();
        tags.clear();
        stacks.clear();

        Optional.ofNullable(AdvancementRewards.Builder.recipe(id).build()).ifPresent(builder::rewards);
        return builder;
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Helper.generateRecipeAdvancements(server);
            server.getPlayerManager().getPlayerList().forEach(entity -> server.getPlayerManager().getAdvancementTracker(entity).reload(server.getAdvancementLoader()));
        });
    }
}

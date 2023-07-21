package me.melontini.andromeda.util;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import me.melontini.andromeda.Andromeda;
import me.melontini.dark_matter.util.MakeSure;
import me.melontini.dark_matter.util.Utilities;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementPositioner;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class MiscUtil {
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
    public static final Map<RecipeType<?>, BiConsumer<Map<Identifier, Advancement.Builder>, Recipe<?>>> RECIPE_TYPE_HANDLERS = Utilities.consume(new ConcurrentHashMap<>(), hashMap -> {
        hashMap.put(RecipeType.BLASTING, (map, recipe) -> map.put(new Identifier(recipe.getId().getNamespace(), "recipes/gen/blasting/" + recipe.getId().toString().replace(":", "_")), MiscUtil.createAdvBuilder(recipe.getId(), recipe.getIngredients().get(0))));
        hashMap.put(RecipeType.SMOKING, (map, recipe) -> map.put(new Identifier(recipe.getId().getNamespace(), "recipes/gen/smoking/" + recipe.getId().toString().replace(":", "_")), MiscUtil.createAdvBuilder(recipe.getId(), recipe.getIngredients().get(0))));
        hashMap.put(RecipeType.SMELTING, (map, recipe) -> map.put(new Identifier(recipe.getId().getNamespace(), "recipes/gen/smelting/" + recipe.getId().toString().replace(":", "_")), MiscUtil.createAdvBuilder(recipe.getId(), recipe.getIngredients().get(0))));
        hashMap.put(RecipeType.CAMPFIRE_COOKING, (map, recipe) -> map.put(new Identifier(recipe.getId().getNamespace(), "recipes/gen/campfire_cooking/" + recipe.getId().toString().replace(":", "_")), MiscUtil.createAdvBuilder(recipe.getId(), recipe.getIngredients().get(0))));
        hashMap.put(RecipeType.STONECUTTING, (map, recipe) -> map.put(new Identifier(recipe.getId().getNamespace(), "recipes/gen/stonecutting/" + recipe.getId().toString().replace(":", "_")), MiscUtil.createAdvBuilder(recipe.getId(), recipe.getIngredients().get(0))));
        hashMap.put(RecipeType.CRAFTING, (map, recipe) -> {
            if (!(recipe instanceof SpecialCraftingRecipe)) {
                if (!recipe.getIngredients().isEmpty()) {
                    map.put(new Identifier(recipe.getId().getNamespace(), "recipes/gen/crafting/" + recipe.getId().toString().replace(":", "_")), MiscUtil.createAdvBuilder(recipe.getId(), recipe.getIngredients().toArray(Ingredient[]::new)));
                }
            }
        });
    });

    public static double horizontalDistanceTo(Vec3d owner, Vec3d target) {
        double d = target.x - owner.x;
        double f = target.z - owner.z;
        return Math.sqrt(d * d + f * f);
    }

    public static String blockPosAsString(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    public static String vec3dAsString(Vec3d vec3d) {
        return vec3d.getX() + ", " + vec3d.getY() + ", " + vec3d.getZ();
    }

    public static void generateRecipeAdvancements(MinecraftServer server) {
        Map<Identifier, Advancement.Builder> advancementBuilders = new ConcurrentHashMap<>();
        AtomicInteger count = new AtomicInteger();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<List<Recipe<?>>> lists = Lists.partition(server.getRecipeManager().values().stream().toList(), 1000);
        for (List<Recipe<?>> list : lists) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (Recipe<?> recipe : list) {
                    if (Andromeda.CONFIG.autogenRecipeAdvancements.blacklistedRecipeNamespaces.contains(recipe.getId().getNamespace()))
                        continue;
                    if (Andromeda.CONFIG.autogenRecipeAdvancements.blacklistedRecipeIds.contains(recipe.getId().toString()))
                        continue;
                    if (recipe.isIgnoredInRecipeBook() && Andromeda.CONFIG.autogenRecipeAdvancements.ignoreRecipesHiddenInTheRecipeBook)
                        continue;

                    if (RECIPE_TYPE_HANDLERS.get(recipe.getType()) != null) {
                        count.getAndIncrement();
                        RECIPE_TYPE_HANDLERS.get(recipe.getType()).accept(advancementBuilders, recipe);
                    } else {
                        if (!recipe.getIngredients().isEmpty()) {
                            count.getAndIncrement();
                            advancementBuilders.put(new Identifier(recipe.getId().getNamespace(), "recipes/gen/generic/" + recipe.getId().toString().replace(":", "_")), MiscUtil.createAdvBuilder(recipe.getId(), recipe.getIngredients().toArray(Ingredient[]::new)));
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

        AndromedaLog.info("finished hacking-in {} recipe advancements", count.get());
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
                        predicates.add(new ItemPredicate(null, Set.of(stackEntry.stack.getItem()), NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, new EnchantmentPredicate[0], new EnchantmentPredicate[0], null, stackEntry.stack.getNbt() != null ? new NbtPredicate(stackEntry.stack.getNbt()) : NbtPredicate.ANY));
                    }
                } else if (entry instanceof Ingredient.TagEntry tagEntry) {
                    if (tags.contains(tagEntry.tag)) continue;
                    tags.add(tagEntry.tag);
                    names.add(String.valueOf(i));
                    predicates.add(new ItemPredicate(tagEntry.tag, null, NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, new EnchantmentPredicate[0], new EnchantmentPredicate[0], null, NbtPredicate.ANY));
                } else {
                    AndromedaLog.error("unknown ingredient found in {}", id);
                }
            }
            builder.criterion(String.valueOf(i), new InventoryChangedCriterion.Conditions(EntityPredicate.Extended.EMPTY, NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, predicates.toArray(ItemPredicate[]::new)));
        }
        builder.criterion("has_recipe", new RecipeUnlockedCriterion.Conditions(EntityPredicate.Extended.EMPTY, id));

        String[][] reqs;
        if (Andromeda.CONFIG.autogenRecipeAdvancements.requireAllItems) {
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

        builder.rewards(new AdvancementRewards(0, new Identifier[0], new Identifier[]{id}, CommandFunction.LazyContainer.EMPTY));
        return builder;
    }
}

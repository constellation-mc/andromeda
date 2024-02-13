package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;

import java.util.Arrays;
import java.util.List;

@ModuleInfo(name = "recipe_advancements_generation", category = "misc", environment = Environment.SERVER)
public class AdvancementGeneration extends Module<AdvancementGeneration.Config> {

    AdvancementGeneration() {
    }

    public static class Config extends BaseConfig {

        public boolean requireAllItems = true;

        public boolean ignoreRecipesHiddenInTheRecipeBook = true;

        public List<String> namespaceBlacklist = Arrays.asList("minecraft", "andromeda", "extshape");

        public List<String> recipeBlacklist = Arrays.asList();
    }
}

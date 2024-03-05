package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

import java.util.Arrays;
import java.util.List;

@ModuleInfo(name = "recipe_advancements_generation", category = "misc", environment = Environment.SERVER)
public class AdvancementGeneration extends Module<AdvancementGeneration.Config> {

    AdvancementGeneration() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }

    public static class Config extends BaseConfig {

        public boolean requireAllItems = true;

        public boolean ignoreRecipesHiddenInTheRecipeBook = true;

        public List<String> namespaceBlacklist = Arrays.asList("minecraft", "andromeda", "extshape");

        public List<String> recipeBlacklist = Arrays.asList();
    }
}

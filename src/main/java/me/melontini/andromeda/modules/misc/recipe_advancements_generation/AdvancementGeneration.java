package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.registries.Common;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Arrays;
import java.util.List;

@ModuleTooltip
@FeatureEnvironment(Environment.SERVER)
public class AdvancementGeneration implements Module<AdvancementGeneration.Config> {

    @Override
    public void onMain() {
        Common.bootstrap(Helper.class);
    }

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public boolean requireAllItems = true;

        @ConfigEntry.Gui.Tooltip
        public boolean ignoreRecipesHiddenInTheRecipeBook = true;

        @ConfigEntry.Gui.Tooltip
        public List<String> namespaceBlacklist = Arrays.asList("minecraft", "andromeda", "extshape");

        @ConfigEntry.Gui.Tooltip
        public List<String> recipeBlacklist = Arrays.asList();
    }
}

package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import me.melontini.andromeda.config.BasicConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Arrays;
import java.util.List;

public class Config extends BasicConfig {

    @ConfigEntry.Gui.Tooltip
    public boolean requireAllItems = true;

    @ConfigEntry.Gui.Tooltip
    public boolean ignoreRecipesHiddenInTheRecipeBook = true;

    @ConfigEntry.Gui.Tooltip
    public List<String> namespaceBlacklist = Arrays.asList("minecraft", "andromeda", "extshape");

    @ConfigEntry.Gui.Tooltip
    public List<String> recipeBlacklist = Arrays.asList();
}

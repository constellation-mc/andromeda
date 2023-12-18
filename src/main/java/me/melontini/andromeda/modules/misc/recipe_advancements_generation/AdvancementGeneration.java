package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.util.JsonOps;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ModuleTooltip
@ModuleInfo(name = "recipe_advancements_generation", category = "misc", environment = Environment.SERVER)
public class AdvancementGeneration extends Module<AdvancementGeneration.Config> {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("autogenRecipeAdvancements")) {
            JsonObject ara = config.getAsJsonObject("autogenRecipeAdvancements");

            JsonOps.ifPresent(ara, "autogenRecipeAdvancements", e -> this.config().enabled = e.getAsBoolean());
            JsonOps.ifPresent(ara, "requireAllItems", e -> this.config().requireAllItems = e.getAsBoolean());
            JsonOps.ifPresent(ara, "ignoreRecipesHiddenInTheRecipeBook", e -> this.config().ignoreRecipesHiddenInTheRecipeBook = e.getAsBoolean());

            JsonOps.ifPresent(ara, "blacklistedRecipeNamespaces", element -> {
                List<String> nsbl = new ArrayList<>();
                for (JsonElement e : element.getAsJsonArray()) {
                    nsbl.add(e.getAsString());
                }
                this.config().namespaceBlacklist = nsbl;
            });

            JsonOps.ifPresent(ara, "blacklistedRecipeIds", element -> {
                List<String> rbbl = new ArrayList<>();
                for (JsonElement e : element.getAsJsonArray()) {
                    rbbl.add(e.getAsString());
                }
                this.config().recipeBlacklist = rbbl;
            });
        }
    }

    @Override
    public void onMain() {
        Common.bootstrap(this, Helper.class);
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

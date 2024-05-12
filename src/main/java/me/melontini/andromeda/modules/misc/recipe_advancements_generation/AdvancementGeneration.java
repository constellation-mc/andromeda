package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ModuleInfo(name = "recipe_advancements_generation", category = "misc", environment = Environment.SERVER)
public final class AdvancementGeneration extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    AdvancementGeneration() {
        this.defineConfig(ConfigState.MAIN, CONFIG);
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }

    @ToString
    public static final class Config extends BaseConfig {
        public boolean requireAllItems = true;
        public boolean ignoreRecipesHiddenInTheRecipeBook = true;
        public List<String> namespaceBlacklist = Arrays.asList("minecraft", "andromeda", "extshape");
        public List<Identifier> recipeBlacklist = new ArrayList<>();
    }
}

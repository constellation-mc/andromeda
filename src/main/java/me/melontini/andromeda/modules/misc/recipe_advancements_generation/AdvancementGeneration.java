package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.common.Andromeda;
import net.minecraft.resources.ResourceLocation;

@ModuleInfo(name = "recipe_advancements_generation", category = "misc")
public final class AdvancementGeneration extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  AdvancementGeneration() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.MAIN).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> () -> Main.init(this, Andromeda.MAIN.get(CONFIG)));
  }

  public static final class Config extends BaseConfig {
    public boolean requireAllItems = true;
    public boolean ignoreRecipesHiddenInTheRecipeBook = true;
    public List<String> namespaceBlacklist = Arrays.asList("minecraft", "andromeda", "extshape");
    public List<ResourceLocation> recipeBlacklist = new ArrayList<>();
  }
}

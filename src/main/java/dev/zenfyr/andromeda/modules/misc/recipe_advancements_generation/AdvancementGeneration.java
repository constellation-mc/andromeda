package dev.zenfyr.andromeda.modules.misc.recipe_advancements_generation;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.Andromeda;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

@ModuleInfo(name = "recipe_advancements_generation", category = "misc", env = Environment.SERVER)
public final class AdvancementGeneration extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  AdvancementGeneration() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.MAIN).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(
        () -> () -> AdvancementGenerationMain.init(this, Andromeda.MAIN.get(CONFIG)));
  }

  public static final class Config extends BaseConfig {
    public boolean requireAllItems = true;
    public boolean ignoreRecipesHiddenInTheRecipeBook = true;
    public List<String> namespaceBlacklist = Arrays.asList("minecraft", "andromeda", "extshape");
    public List<ResourceLocation> recipeBlacklist = new ArrayList<>();
  }
}

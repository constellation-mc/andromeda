package dev.zenfyr.andromeda.modules.blocks.fletching_table_tweaks;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;

@ModuleInfo(name = "fletching_table_tweaks", category = "blocks")
public final class FletchingTableTweaks extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  FletchingTableTweaks() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> FletchingScreenHandler::init);
    InitEvents.CLIENT.listen(() -> FletchingScreen::onClient);
  }

  public static final class Config extends BaseConfig {
    public float divergenceModifier = 0.2f;
  }
}

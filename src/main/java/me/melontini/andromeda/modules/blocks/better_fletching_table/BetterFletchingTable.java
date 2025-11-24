package me.melontini.andromeda.modules.blocks.better_fletching_table;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;

@ModuleInfo(name = "better_fletching_table", category = "blocks")
public final class BetterFletchingTable extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  BetterFletchingTable() {
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

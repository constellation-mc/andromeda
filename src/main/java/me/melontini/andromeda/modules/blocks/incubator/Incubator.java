package me.melontini.andromeda.modules.blocks.incubator;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.common.util.commander.CommanderSupport;

@ModuleInfo(name = "incubator", category = "blocks")
public final class Incubator extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Incubator() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);

    CommanderSupport.require(this);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> IncubatorBlock::init);
    InitEvents.CLIENT.listen(() -> IncubatorBlockRenderer::onClient);
  }

  public static class Config extends BaseConfig {

    // TODO @SpecialEnvironment(Environment.SERVER)
    public boolean randomness = true;
  }
}

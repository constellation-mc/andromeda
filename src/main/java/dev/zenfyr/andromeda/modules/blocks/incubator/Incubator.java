package dev.zenfyr.andromeda.modules.blocks.incubator;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.common.util.CommanderSupport;

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

package me.melontini.andromeda.modules.blocks.incubator;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.util.commander.CommanderSupport;

@ModuleInfo(name = "incubator", category = "blocks")
public final class Incubator extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Incubator() {
    this.defineConfig(ConfigState.GAME, CONFIG);
    InitEvent.main(this).listen(() -> () -> IncubatorBlock.init(this));
    InitEvent.client(this).listen(() -> IncubatorBlockRenderer::onClient);

    CommanderSupport.require(this);
  }

  @ToString
  public static class Config extends BaseConfig {

    @SpecialEnvironment(Environment.SERVER)
    public boolean randomness = true;
  }
}

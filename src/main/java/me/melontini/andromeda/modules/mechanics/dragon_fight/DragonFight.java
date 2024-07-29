package me.melontini.andromeda.modules.mechanics.dragon_fight;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;

@ModuleInfo(name = "dragon_fight", category = "mechanics", environment = Environment.SERVER)
public final class DragonFight extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  DragonFight() {
    this.defineConfig(ConfigState.MAIN, CONFIG);
    InitEvent.main(this).listen(() -> EnderDragonManager::init);
  }

  @ToString
  public static final class Config extends BaseConfig {
    public boolean respawnCrystals = true;
    public boolean scaleHealthByMaxPlayers = false;
    public boolean shorterCrystalTrackRange = true;
    public boolean shorterSpikes = false;
  }
}

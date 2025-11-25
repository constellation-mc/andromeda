package dev.zenfyr.andromeda.modules.mechanics.villager_gifting;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.config.GameConfig;

@ModuleInfo(name = "villager_gifting", category = "mechanics", env = Environment.SERVER)
public final class VillagerGifting extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  VillagerGifting() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}

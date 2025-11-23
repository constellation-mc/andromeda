package me.melontini.andromeda.modules.mechanics.villager_gifting;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "villager_gifting", category = "mechanics")
public final class VillagerGifting extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  VillagerGifting() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }
}

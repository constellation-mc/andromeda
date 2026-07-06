package dev.zenfyr.andromeda.modules.items.fire_damage;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.config.GameConfig;

@ModuleInfo(name = "fire_damage", category = "items", env = Environment.SERVER)
public class FireDamage extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<GameConfig> CONFIG = ConfigDefinition.game();

  public FireDamage() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> FireDamageMain::init);
  }
}

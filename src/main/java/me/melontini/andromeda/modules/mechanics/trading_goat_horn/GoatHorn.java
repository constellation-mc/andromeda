package me.melontini.andromeda.modules.mechanics.trading_goat_horn;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;
import net.minecraft.resources.ResourceLocation;

@ModuleInfo(name = "trading_goat_horn", category = "mechanics", env = Environment.SERVER)
public final class GoatHorn extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  GoatHorn() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> CustomTraderManager::init);
  }

  public static class Config extends GameConfig {
    public int cooldown = 48000;
    public ResourceLocation instrumentId = ResourceLocation.tryBuild("minecraft", "sing_goat_horn");
    public boolean highlightTrader = false;
  }
}

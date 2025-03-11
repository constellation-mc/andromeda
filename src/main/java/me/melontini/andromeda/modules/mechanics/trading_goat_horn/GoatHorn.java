package me.melontini.andromeda.modules.mechanics.trading_goat_horn;

import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;
import me.melontini.andromeda.common.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.common.util.commander.number.LongIntermediary;
import net.minecraft.util.Identifier;

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
    public LongIntermediary cooldown = LongIntermediary.of(48000);
    public Identifier instrumentId = Identifier.of("minecraft", "sing_goat_horn");
    public BooleanIntermediary highlightTrader = BooleanIntermediary.of(false);
  }
}

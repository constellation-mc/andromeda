package me.melontini.andromeda.modules.mechanics.trading_goat_horn;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.util.commander.number.LongIntermediary;
import net.minecraft.util.Identifier;

@ModuleInfo(name = "trading_goat_horn", category = "mechanics", environment = Environment.SERVER)
public final class GoatHorn extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  GoatHorn() {
    this.defineConfig(ConfigState.GAME, CONFIG);
    InitEvent.main(this).listen(() -> CustomTraderManager::init);
  }

  public static class Config extends GameConfig {
    public LongIntermediary cooldown = LongIntermediary.of(48000);
    public Identifier instrumentId = Identifier.of("minecraft", "sing_goat_horn");
  }
}

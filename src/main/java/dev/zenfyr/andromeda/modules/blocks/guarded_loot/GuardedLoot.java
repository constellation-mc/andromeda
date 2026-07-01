package dev.zenfyr.andromeda.modules.blocks.guarded_loot;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.common.config.GameConfig;
import java.util.Optional;

@ModuleInfo(name = "guarded_loot", category = "blocks")
public final class GuardedLoot extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  GuardedLoot() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> GuardedLootMain::init);
  }

  public static class Config extends GameConfig {
    public double range = 4;
    public boolean allowLockPicking = true;
    public BreakingHandler breakingHandler = BreakingHandler.UNBREAKABLE;
    public boolean checkReach = false;
  }

  public enum BreakingHandler /*implements TranslationKeyProvider*/ {
    NONE,
    UNBREAKABLE;

    // @Override
    public Optional<String> getTranslationKey() {
      return Optional.of("config.andromeda.blocks.guarded_loot.option.BreakingHandler." + name());
    }
  }
}

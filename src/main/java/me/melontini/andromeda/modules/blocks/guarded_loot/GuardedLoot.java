package me.melontini.andromeda.modules.blocks.guarded_loot;

import java.util.Optional;
import me.melontini.andromeda.api.ModuleDeclarations;
import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.DeclareApiEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.common.config.GameConfig;

@ModuleInfo(name = "guarded_loot", category = "blocks")
public final class GuardedLoot extends Module implements PostBootstrapEvent {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  GuardedLoot() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
    DeclareApiEvent.BUS.listen(consumer -> consumer.accept(ModuleDeclarations.LOOT_UNLOCKER));
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> Main::init);
  }

  public static class Config extends GameConfig {
    public double range = 4;
    public boolean allowLockPicking = true;
    public BreakingHandler breakingHandler = BreakingHandler.UNBREAKABLE;
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

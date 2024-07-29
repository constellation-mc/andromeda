package me.melontini.andromeda.modules.blocks.guarded_loot;

import java.util.Optional;
import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.common.util.TranslationKeyProvider;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;

@ModuleInfo(name = "guarded_loot", category = "blocks")
public final class GuardedLoot extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  GuardedLoot() {
    this.defineConfig(ConfigState.GAME, CONFIG);
    InitEvent.main(this).listen(() -> Main::init);
  }

  @ToString
  public static class Config extends GameConfig {
    public DoubleIntermediary range = DoubleIntermediary.of(4);
    public boolean allowLockPicking = true;
    public BreakingHandler breakingHandler = BreakingHandler.UNBREAKABLE;
  }

  public enum BreakingHandler implements TranslationKeyProvider {
    NONE,
    UNBREAKABLE;

    @Override
    public Optional<String> getTranslationKey() {
      return Optional.of("config.andromeda.blocks.guarded_loot.option.BreakingHandler." + name());
    }
  }
}

package me.melontini.andromeda.modules.misc.tiny_storage;

import java.util.Optional;
import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.bootstrap.util.Environment;

@ModuleInfo(name = "tiny_storage", category = "misc", env = Environment.SERVER)
public final class TinyStorage extends Module implements PostBootstrapEvent {

  public static final ThreadLocal<Boolean> LOADING = ThreadLocal.withInitial(() -> false);
  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  TinyStorage() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.MAIN).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.MAIN.listen(() -> Main::init);
  }

  public static class Config extends BaseConfig {
    public TransferMode transferMode = TransferMode.FOLLOW_GAMERULE;
  }

  public enum TransferMode /*implements TranslationKeyProvider*/ {
    FOLLOW_GAMERULE,
    ALWAYS_TRANSFER;

    // TODO @Override
    public Optional<String> getTranslationKey() {
      return Optional.of("config.andromeda.misc.tiny_storage.option.TransferMode." + name());
    }
  }
}

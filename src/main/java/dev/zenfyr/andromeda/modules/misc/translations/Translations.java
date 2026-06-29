package dev.zenfyr.andromeda.modules.misc.translations;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.CreateBootstrapConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.bootstrap.util.Util;
import dev.zenfyr.pulsar.api.platform.Platform;
import java.nio.file.Path;

@ModuleInfo(name = "translations", category = "misc", env = Environment.CLIENT)
public final class Translations extends Module implements PostBootstrapEvent {

  // the git branch for this version to pull translations from.
  public static final String BRANCH = "26.1.2-fabric";
  public static final Path TRANSLATION_PACK = Util.HIDDEN_PATH.resolve("andromeda_translations");
  public static final Path LANG_PATH = TRANSLATION_PACK.resolve("assets/andromeda/lang");
  public static final Path EN_US = LANG_PATH.resolve("en_us.json");
  public static final Path OPTIONS = Platform.getPlatform().getGameDir().resolve("options.txt");

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Translations() {
    CreateBootstrapConfigEvent.get(this).listen(() -> true);
    RegisterConfigEvent.get(this, RegisterConfigEvent.CLIENT).listen(() -> CONFIG);
  }

  @Override
  public void postBootstrap() {
    InitEvents.CLIENT.listen(() -> Client::init);
  }

  public static final class Config extends BaseConfig {

    public String baseUrl = "default";
  }
}

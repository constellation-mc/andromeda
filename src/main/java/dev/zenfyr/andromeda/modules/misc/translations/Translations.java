package dev.zenfyr.andromeda.modules.misc.translations;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.event.CreateBootstrapConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.bootstrap.event.PostBootstrapEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.util.Util;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

@ModuleInfo(name = "translations", category = "misc", env = Environment.CLIENT)
public final class Translations extends Module implements PostBootstrapEvent {

  // the git branch for this version to pull translations from.
  public static final String BRANCH = "1.20.1-fabric";
  public static final Path TRANSLATION_PACK = Util.HIDDEN_PATH.resolve("andromeda_translations");
  public static final Path LANG_PATH = TRANSLATION_PACK.resolve("assets/andromeda/lang");
  public static final Path EN_US = LANG_PATH.resolve("en_us.json");
  public static final Path OPTIONS = FabricLoader.getInstance().getGameDir().resolve("options.txt");

  Translations() {
    CreateBootstrapConfigEvent.get(this).listen(() -> true);
  }

  @Override
  public void postBootstrap() {
    InitEvents.CLIENT.listen(() -> Client::init);
  }
}

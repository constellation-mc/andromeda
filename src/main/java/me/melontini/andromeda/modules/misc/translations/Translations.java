package me.melontini.andromeda.modules.misc.translations;

import java.nio.file.Path;
import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.event.CreateBootstrapConfigEvent;
import me.melontini.andromeda.bootstrap.event.InitEvents;
import me.melontini.andromeda.bootstrap.event.PostBootstrapEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.util.Util;
import net.fabricmc.loader.api.FabricLoader;

@ModuleInfo(name = "translations", category = "misc", env = Environment.CLIENT)
public final class Translations extends Module implements PostBootstrapEvent {

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

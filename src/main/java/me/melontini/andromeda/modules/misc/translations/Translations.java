package me.melontini.andromeda.modules.misc.translations;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.util.CommonValues;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

@ModuleInfo(name = "translations", category = "misc", environment = Environment.CLIENT)
public final class Translations extends Module {

    public static final Path TRANSLATION_PACK = CommonValues.hiddenPath().resolve("andromeda_translations");
    public static final Path LANG_PATH = TRANSLATION_PACK.resolve("assets/andromeda/lang");
    public static final Path EN_US = LANG_PATH.resolve("en_us.json");
    public static final Path OPTIONS = FabricLoader.getInstance().getGameDir().resolve("options.txt");

    Translations() {
        InitEvent.client(this).listen(() -> () -> Client.init(this));
    }
}

package me.melontini.andromeda.modules.misc.translations;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class Translations implements Module {

    @Override
    public void onClient() {
        TranslationUpdater.checkAndUpdate();
    }

    @Override
    public Environment environment() {
        return Environment.CLIENT;
    }
}

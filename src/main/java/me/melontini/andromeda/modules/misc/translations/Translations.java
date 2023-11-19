package me.melontini.andromeda.modules.misc.translations;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;

@FeatureEnvironment(Environment.CLIENT)
public class Translations implements BasicModule {

    @Override
    public void onClient() {
        TranslationUpdater.checkAndUpdate();
    }
}

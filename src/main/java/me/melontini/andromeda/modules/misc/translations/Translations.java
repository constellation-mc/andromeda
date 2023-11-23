package me.melontini.andromeda.modules.misc.translations;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleTooltip;

@ModuleTooltip
@FeatureEnvironment(Environment.CLIENT)
public class Translations extends BasicModule {

    @Override
    public void onClient() {
        TranslationUpdater.checkAndUpdate();
    }
}

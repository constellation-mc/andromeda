package me.melontini.andromeda.modules.misc.translations;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;

@ModuleTooltip
@ModuleInfo(name = "translations", category = "misc", environment = Environment.CLIENT)
public class Translations extends BasicModule {

    @Override
    public void onClient() {
        TranslationUpdater.checkAndUpdate();
    }
}

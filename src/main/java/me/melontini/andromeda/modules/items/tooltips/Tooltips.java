package me.melontini.andromeda.modules.items.tooltips;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "tooltips", category = "items", environment = Environment.CLIENT)
public class Tooltips extends Module<Tooltips.Config> {

    Tooltips() {
    }

    public static class Config extends BaseConfig {

        public boolean clock = true;
        public boolean compass = true;
        public boolean recoveryCompass = true;
    }
}

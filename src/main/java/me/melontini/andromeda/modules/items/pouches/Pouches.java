package me.melontini.andromeda.modules.items.pouches;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.Unscoped;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Unscoped
@ModuleInfo(name = "pouches", category = "items")
public class Pouches extends Module<Pouches.Config> {

    public static class Config extends BaseConfig {
        @ConfigEntry.Gui.RequiresRestart
        public boolean seedPouch = true;
        @ConfigEntry.Gui.RequiresRestart
        public boolean flowerPouch = true;
        @ConfigEntry.Gui.RequiresRestart
        public boolean saplingPouch = true;
        @ConfigEntry.Gui.RequiresRestart
        public boolean specialPouch = false;
    }
}

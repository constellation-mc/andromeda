package me.melontini.andromeda.modules.items.pouches;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

@Unscoped
@ModuleInfo(name = "pouches", category = "items")
public class Pouches extends Module<Pouches.Config> {

    Pouches() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.merged(this).listen(() -> List.of(Merged.class));
    }

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

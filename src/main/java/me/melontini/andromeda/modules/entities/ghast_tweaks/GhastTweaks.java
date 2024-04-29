package me.melontini.andromeda.modules.entities.ghast_tweaks;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.util.commander.number.NumberIntermediary;

import java.util.List;

@ModuleInfo(name = "ghast_tweaks", category = "entities", environment = Environment.SERVER)
public class GhastTweaks extends Module<GhastTweaks.Config> {

    GhastTweaks() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }

    public static class Config extends Module.BaseConfig {
        public boolean explodeOnDeath = false;
        public NumberIntermediary explosionPower = NumberIntermediary.of(4);
    }
}

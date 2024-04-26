package me.melontini.andromeda.modules.entities.ghast_tweaks;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

import java.util.List;

@ModuleInfo(name = "ghast_tweaks", category = "entities", environment = Environment.SERVER)
public class GhastTweaks extends Module<GhastTweaks.Config> {

    GhastTweaks() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }

    public static class Config extends Module.BaseConfig {
        public boolean explodeOnDeath = false;
        public float explosionPower = 4f;
    }
}

package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

@ModuleInfo(name = "zombie/clean_pickup", category = "entities", environment = Environment.SERVER)
public class Pickup extends Module {

    Pickup() {
        this.defineConfig(ConfigState.GAME, ConfigDefinition.GAME);
    }
}

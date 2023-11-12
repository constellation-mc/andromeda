package me.melontini.andromeda.modules.world.falling_beenests;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class CanBeeNestsFall implements Module {
    @Override
    public Environment environment() {
        return Environment.SERVER;
    }
}

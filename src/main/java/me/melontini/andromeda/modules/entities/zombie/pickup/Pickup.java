package me.melontini.andromeda.modules.entities.zombie.pickup;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class Pickup implements Module {
    @Override
    public Environment environment() {
        return Environment.SERVER;
    }
}

package me.melontini.andromeda.modules.mechanics.dragon_fight;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class DragonFight implements Module {

    @Override
    public Environment environment() {
        return Environment.SERVER;
    }

    @Override
    public Class<?> configClass() {
        return Config.class;
    }
}

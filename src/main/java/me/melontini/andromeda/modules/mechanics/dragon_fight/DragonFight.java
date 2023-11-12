package me.melontini.andromeda.modules.mechanics.dragon_fight;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.config.Environment;

public class DragonFight implements Module {

    @Override
    public Environment environment() {
        return Environment.SERVER;
    }

    @Override
    public boolean enabled() {
        return Config.get().dragonFight.fightTweaks;
    }
}

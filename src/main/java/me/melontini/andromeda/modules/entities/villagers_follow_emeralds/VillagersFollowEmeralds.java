package me.melontini.andromeda.modules.entities.villagers_follow_emeralds;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class VillagersFollowEmeralds implements Module {
    @Override
    public Environment environment() {
        return Environment.SERVER;
    }

    @Override
    public boolean enabled() {
        return false;
    }
}

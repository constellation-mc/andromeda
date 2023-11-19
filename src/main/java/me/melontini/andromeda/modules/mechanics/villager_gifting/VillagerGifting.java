package me.melontini.andromeda.modules.mechanics.villager_gifting;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.util.annotations.config.Environment;

public class VillagerGifting implements Module {

    @Override
    public void onMain() {
        Common.bootstrap(GiftTags.class);
    }

    @Override
    public Environment environment() {
        return Environment.SERVER;
    }
}

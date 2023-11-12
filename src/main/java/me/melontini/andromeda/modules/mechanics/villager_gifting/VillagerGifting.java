package me.melontini.andromeda.modules.mechanics.villager_gifting;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.util.annotations.config.Environment;

public class VillagerGifting implements Module {

    @Override
    public void onMain() {
        GiftTags.init();
    }

    @Override
    public Environment environment() {
        return Environment.SERVER;
    }
}

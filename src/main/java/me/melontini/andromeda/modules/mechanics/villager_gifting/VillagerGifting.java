package me.melontini.andromeda.modules.mechanics.villager_gifting;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.registries.Common;

@ModuleTooltip
@FeatureEnvironment(Environment.SERVER)
public class VillagerGifting extends BasicModule {

    @Override
    public void onMain() {
        Common.bootstrap(GiftTags.class);
    }
}

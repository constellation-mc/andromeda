package me.melontini.andromeda.modules.mechanics.villager_gifting;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.common.registries.Common;

@ModuleTooltip
@ModuleInfo(name = "villager_gifting", category = "mechanics", environment = Environment.SERVER)
public class VillagerGifting extends BasicModule {

    @Override
    public void onMain() {
        Common.bootstrap(this, GiftTags.class);
    }
}

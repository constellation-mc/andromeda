package me.melontini.andromeda.modules.entities.villagers_follow_emeralds;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.common.registries.Common;

@OldConfigKey("villagersFollowEmeraldBlocks")
@ModuleTooltip
@ModuleInfo(name = "villagers_follow_emeralds", category = "entities", environment = Environment.SERVER)
public class VillagersFollowEmeralds extends BasicModule {

    @Override
    public void onMain() {
        Common.bootstrap(this, VillagerTemptGoal.class);
    }
}

package me.melontini.andromeda.modules.mechanics.villager_gifting;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.registries.Common;

@OldConfigKey("villagerGifting")
@ModuleInfo(name = "villager_gifting", category = "mechanics", environment = Environment.SERVER)
public class VillagerGifting extends Module<BasicConfig> {

    @Override
    public void onMain() {
        Common.bootstrap(this, GiftTags.class);
    }
}

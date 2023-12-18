package me.melontini.andromeda.modules.misc.damage_backport;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.common.registries.Common;

@OldConfigKey("damageBackport")
@ModuleTooltip
@ModuleInfo(name = "damage_backport", category = "misc", environment = Environment.SERVER)
public class DamageBackport extends BasicModule {

    @Override
    public void onMain() {
        Common.bootstrap(this, DamageCommand.class);
    }
}

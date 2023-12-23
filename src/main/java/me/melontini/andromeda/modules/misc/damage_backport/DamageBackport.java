package me.melontini.andromeda.modules.misc.damage_backport;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.registries.Common;

@OldConfigKey("damageBackport")
@ModuleInfo(name = "damage_backport", category = "misc", environment = Environment.SERVER)
public class DamageBackport extends Module<BasicConfig> {

    @Override
    public void onMain() {
        Common.bootstrap(this, DamageCommand.class);
    }
}

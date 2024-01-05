package me.melontini.andromeda.modules.misc.damage_backport;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.annotations.Unscoped;
import me.melontini.andromeda.common.registries.Common;

@Unscoped
@OldConfigKey("damageBackport")
@ModuleInfo(name = "damage_backport", category = "misc", environment = Environment.SERVER)
public class DamageBackport extends Module<Module.BaseConfig> {

    @Override
    public void onMain() {
        Common.bootstrap(this, DamageCommand.class);
    }
}

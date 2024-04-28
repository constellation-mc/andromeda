package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.modules.items.lockpick.client.Client;
import me.melontini.dark_matter.api.base.util.MathUtil;

import java.util.List;

@Unscoped
@ModuleInfo(name = "lockpick", category = "items")
public class Lockpick extends Module<Lockpick.Config> {

    Lockpick() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }

    public boolean rollLockpick() {
        var c = Andromeda.getConfig(this).c;
        return c.chance - 1 == 0 || MathUtil.threadRandom().nextInt(c.chance - 1) == 0;
    }

    public static class Config extends BaseConfig {

        public int chance = 3;

        public boolean breakAfterUse = true;

        public boolean villagerInventory = true;
    }
}

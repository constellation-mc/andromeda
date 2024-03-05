package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.modules.items.lockpick.client.Client;
import me.melontini.dark_matter.api.base.util.MathStuff;

import java.util.List;

@Unscoped
@ModuleInfo(name = "lockpick", category = "items")
public class Lockpick extends Module<Lockpick.Config> {

    Lockpick() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }

    public boolean rollLockpick() {
        return this.config().chance - 1 == 0 || MathStuff.threadRandom().nextInt(this.config().chance - 1) == 0;
    }

    public static class Config extends BaseConfig {

        public int chance = 3;

        public boolean breakAfterUse = true;

        public boolean villagerInventory = true;
    }
}

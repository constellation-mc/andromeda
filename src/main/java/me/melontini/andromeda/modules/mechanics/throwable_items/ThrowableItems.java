package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.modules.mechanics.throwable_items.client.Client;

import java.util.List;

@Unscoped
@ModuleInfo(name = "throwable_items", category = "mechanics")
public class ThrowableItems extends Module<ThrowableItems.Config> {

    ThrowableItems() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }

    public static class Config extends BaseConfig {

        @SpecialEnvironment(Environment.SERVER)
        public boolean canZombiesThrowItems = true;

        @SpecialEnvironment(Environment.SERVER)
        public int zombieThrowInterval = 40;

        @SpecialEnvironment(Environment.BOTH)
        public boolean tooltip = true;
    }
}

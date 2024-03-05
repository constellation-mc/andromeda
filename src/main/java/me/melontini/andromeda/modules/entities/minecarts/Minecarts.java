package me.melontini.andromeda.modules.entities.minecarts;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.modules.entities.minecarts.client.Client;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

@Unscoped
@ModuleInfo(name = "minecarts", category = "entities")
public class Minecarts extends Module<Minecarts.Config> {

    Minecarts() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }

    public static class Config extends BaseConfig {

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isAnvilMinecartOn = false;

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isNoteBlockMinecartOn = false;

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isJukeboxMinecartOn = false;

        @Unscoped
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isSpawnerMinecartOn = false;
    }
}

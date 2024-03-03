package me.melontini.andromeda.modules.blocks.incubator;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.modules.blocks.incubator.client.Client;

import java.util.List;

@Unscoped
@ModuleInfo(name = "incubator", category = "blocks")
public class Incubator extends Module<Incubator.Config> {

    Incubator() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }

    public static class Config extends BaseConfig {

        @SpecialEnvironment(Environment.SERVER)
        public boolean randomness = true;
    }
}

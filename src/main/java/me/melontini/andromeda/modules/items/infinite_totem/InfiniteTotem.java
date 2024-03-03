package me.melontini.andromeda.modules.items.infinite_totem;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.modules.items.infinite_totem.client.Client;

import java.util.List;

@ModuleInfo(name = "infinite_totem", category = "items")
public class InfiniteTotem extends Module<InfiniteTotem.Config> {

    InfiniteTotem() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }

    public static class Config extends BaseConfig {

        public boolean enableAscension = true;
    }
}

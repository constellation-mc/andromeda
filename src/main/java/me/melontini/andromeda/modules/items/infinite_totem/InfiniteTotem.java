package me.melontini.andromeda.modules.items.infinite_totem;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;

@ModuleInfo(name = "infinite_totem", category = "items")
public class InfiniteTotem extends Module<InfiniteTotem.Config> {

    InfiniteTotem() {
    }

    public static class Config extends BaseConfig {

        public boolean enableAscension = true;
    }
}

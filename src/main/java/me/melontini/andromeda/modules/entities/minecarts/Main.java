package me.melontini.andromeda.modules.entities.minecarts;

import me.melontini.andromeda.common.Andromeda;

public class Main {

    Main(Minecarts module) {
        MinecartItems.init(module, Andromeda.getConfig(module).c);
        MinecartEntities.init(Andromeda.getConfig(module).c);
    }
}

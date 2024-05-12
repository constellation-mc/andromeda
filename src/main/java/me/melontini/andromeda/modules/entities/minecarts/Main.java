package me.melontini.andromeda.modules.entities.minecarts;


public final class Main {

    Main(Minecarts module, Minecarts.Config config) {
        MinecartItems.init(module, config);
        MinecartEntities.init(config);
    }
}

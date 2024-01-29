package me.melontini.andromeda.modules.entities.minecarts;

public class Main {

    Main(Minecarts module) {
        MinecartItems.init(module, module.config());
        MinecartEntities.init(module.config());
    }
}

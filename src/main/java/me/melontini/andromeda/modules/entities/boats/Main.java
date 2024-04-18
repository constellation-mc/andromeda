package me.melontini.andromeda.modules.entities.boats;

public class Main {

    Main(Boats module) {
        BoatItems.init(module, module.config());
        BoatEntities.init(module.config());
    }
}

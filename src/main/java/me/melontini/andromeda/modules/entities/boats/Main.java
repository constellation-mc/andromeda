package me.melontini.andromeda.modules.entities.boats;


public final class Main {

    Main(Boats module, Boats.Config config) {
        BoatItems.init(module, config);
        BoatEntities.init(config);
    }
}

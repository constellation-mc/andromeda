package me.melontini.andromeda.modules.entities.boats;

public class Main {

    Main(Boats module) {
        BoatItems.init(module);
        BoatEntities.init(module.config());
    }
}

package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.common.Andromeda;

public class Main {

    Main(Boats module) {
        BoatItems.init(module, Andromeda.getConfig(module).c);
        BoatEntities.init(Andromeda.getConfig(module).c);
    }
}

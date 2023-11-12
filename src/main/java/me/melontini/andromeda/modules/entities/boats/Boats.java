package me.melontini.andromeda.modules.entities.boats;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.modules.entities.boats.client.Client;
import me.melontini.andromeda.registries.Common;

public class Boats implements Module {

    @Override
    public void onClient() {
        Common.bootstrap(Client.class);
    }

    @Override
    public void onMain() {
        Common.bootstrap(BoatItems.class);
        Common.bootstrap(BoatEntities.class);
    }

    @Override
    public boolean enabled() {
        return true;
    }
}

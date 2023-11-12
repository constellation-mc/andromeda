package me.melontini.andromeda.modules.blocks.better_fletching_table;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.modules.blocks.better_fletching_table.client.Client;
import me.melontini.andromeda.registries.Common;

public class BetterFletchingTable implements Module {

    @Override
    public void onClient() {
        Common.bootstrap(Client.class);
    }

    @Override
    public void onMain() {
        Common.bootstrap(Content.class);
    }
}

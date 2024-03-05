package me.melontini.andromeda.modules.blocks.better_fletching_table;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.modules.blocks.better_fletching_table.client.Client;

import java.util.List;

@Unscoped
@ModuleInfo(name = "better_fletching_table", category = "blocks")
public class BetterFletchingTable extends Module<Module.BaseConfig> {

    BetterFletchingTable() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }
}

package me.melontini.andromeda.modules.gui.item_frame_tooltips;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.events.ConfigEvent;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.modules.gui.item_frame_tooltips.client.Client;

import java.util.List;
import java.util.function.BooleanSupplier;

import static me.melontini.andromeda.base.Bootstrap.testModVersion;

@ModuleInfo(name = "item_frame_tooltips", category = "gui", environment = Environment.CLIENT)
public class ItemFrameTooltips extends Module<Module.BaseConfig> {

    ItemFrameTooltips() {
        InitEvent.client(this).listen(() -> List.of(Client.class));
        BooleanSupplier iceberg = () -> testModVersion(this, "minecraft", ">=1.20") && testModVersion(this, "iceberg", "<1.1.13");

        ConfigEvent.forModule(this).listen(manager -> {
            manager.onSave((config, path) -> {
                if (iceberg.getAsBoolean()) config.enabled = false;
            });
        });
        BlockadesEvent.BUS.listen(blockade -> {
            blockade.explain(this, "enabled", iceberg, blockade.andromeda("iceberg"));
        });
    }
}

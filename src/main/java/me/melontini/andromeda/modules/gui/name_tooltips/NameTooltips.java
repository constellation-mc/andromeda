package me.melontini.andromeda.modules.gui.name_tooltips;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.events.ConfigEvent;

import java.util.function.BooleanSupplier;

import static me.melontini.andromeda.base.Bootstrap.testModVersion;

@ModuleInfo(name = "name_tooltips", category = "gui", environment = Environment.CLIENT)
public class NameTooltips extends Module<Module.BaseConfig> {

    NameTooltips() {
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

package me.melontini.andromeda.modules.gui.name_tooltips;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.common.client.config.FeatureBlockade;
import me.melontini.dark_matter.api.base.config.ConfigManager;

import java.util.function.BooleanSupplier;

import static me.melontini.andromeda.base.Bootstrap.testModVersion;

@OldConfigKey("tooltipNotName")
@ModuleInfo(name = "name_tooltips", category = "gui", environment = Environment.CLIENT)
public class NameTooltips extends Module<Module.BaseConfig> {

    private final BooleanSupplier iceberg = () -> testModVersion(this, "minecraft", ">=1.20") && testModVersion(this, "iceberg", "<1.1.13");

    @Override
    public void onConfig(ConfigManager<BaseConfig> manager) {
        manager.onSave((config, path) -> {
            if (iceberg.getAsBoolean()) config.enabled = false;
        });
    }

    @Override
    public void collectBlockades() {
        FeatureBlockade.get().explain(this, "enabled", iceberg,
                "andromeda.config.option_manager.reason.andromeda.iceberg");
    }
}

package me.melontini.andromeda.modules.gui.name_tooltips;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.client.config.FeatureBlockade;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import net.minecraft.text.Text;

import static me.melontini.andromeda.base.Bootstrap.testModVersion;

@OldConfigKey("tooltipNotName")
@ModuleTooltip
@ModuleInfo(name = "name_tooltips", category = "gui", environment = Environment.CLIENT)
public class NameTooltips extends BasicModule {

    @Override
    public void onConfig(ConfigManager<BasicConfig> manager) {
        manager.onLoad(config -> {
            if (testModVersion("minecraft", ">=1.20") &&
                    testModVersion("iceberg", "<1.1.13")) {
                config.enabled = false;
            }
        });
    }

    @Override
    public void collectBlockades() {
        FeatureBlockade.get().explain(this, "enabled", () ->
                        testModVersion("minecraft", ">=1.20") &&
                                testModVersion("iceberg", "<1.1.13"),
                Text.translatable("andromeda.config.option_manager.reason.andromeda.iceberg"));
    }
}

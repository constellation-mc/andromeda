package me.melontini.andromeda.modules.gui.item_frame_tooltips;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.common.client.config.FeatureBlockade;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;

import static me.melontini.andromeda.base.Bootstrap.testModVersion;

@OldConfigKey("itemFrameTooltips")
@ModuleInfo(name = "item_frame_tooltips", category = "gui", environment = Environment.CLIENT)
public class ItemFrameTooltips extends Module<BasicConfig> {

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
                TextUtil.translatable("andromeda.config.option_manager.reason.andromeda.iceberg"));
    }
}

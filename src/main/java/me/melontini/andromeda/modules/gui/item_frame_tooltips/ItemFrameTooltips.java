package me.melontini.andromeda.modules.gui.item_frame_tooltips;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.OldConfigKey;
import me.melontini.andromeda.common.client.config.FeatureBlockade;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;

import java.util.function.BooleanSupplier;

import static me.melontini.andromeda.base.Bootstrap.testModVersion;

@OldConfigKey("itemFrameTooltips")
@ModuleInfo(name = "item_frame_tooltips", category = "gui", environment = Environment.CLIENT)
public class ItemFrameTooltips extends Module<Module.BaseConfig> {

    private final BooleanSupplier iceberg = () -> testModVersion(this, "minecraft", ">=1.20") && testModVersion(this, "iceberg", "<1.1.13");

    @Override
    public void onConfig(ConfigManager<BaseConfig> manager) {
        manager.onLoad(config -> {
            if (iceberg.getAsBoolean()) config.enabled = false;
        });
    }

    @Override
    public void collectBlockades() {
        FeatureBlockade.get().explain(this, "enabled", iceberg,
                TextUtil.translatable("andromeda.config.option_manager.reason.andromeda.iceberg"));
    }
}

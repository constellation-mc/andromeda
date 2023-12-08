package me.melontini.andromeda.modules.gui.name_tooltips;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.config.ConfigBuilder;

import java.util.Map;

import static me.melontini.andromeda.util.MiscUtil.testModVersion;

@SuppressWarnings("UnstableApiUsage")
@ModuleTooltip
@ModuleInfo(name = "name_tooltips", category = "gui", environment = Environment.CLIENT)
public class NameTooltips extends BasicModule {

    @Override
    public void onConfig(ConfigBuilder<BasicConfig> builder) {
        builder.processors((registry, mod) -> registry.register(CommonValues.MODID + ":iceberg", manager -> {
            if (testModVersion("minecraft", ">=1.20") &&
                    testModVersion("iceberg", "<1.1.13")) {
                return Map.of("enabled", false);
            }
            return null;
        }, mod));
    }
}

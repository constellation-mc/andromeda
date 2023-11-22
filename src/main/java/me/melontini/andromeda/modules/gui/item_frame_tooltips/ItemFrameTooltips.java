package me.melontini.andromeda.modules.gui.item_frame_tooltips;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.config.OptionProcessorRegistry;
import net.fabricmc.loader.api.ModContainer;

import java.util.Map;

import static me.melontini.andromeda.util.MiscUtil.testModVersion;

@SuppressWarnings("UnstableApiUsage")
@ModuleTooltip
@FeatureEnvironment(Environment.CLIENT)
public class ItemFrameTooltips implements BasicModule {

    @Override
    public void onProcessors(OptionProcessorRegistry<BasicConfig> registry, ModContainer mod) {
        registry.register(CommonValues.MODID + ":iceberg", manager -> {
            if (testModVersion("minecraft", ">=1.20") &&
                    testModVersion("iceberg", "<1.1.13")) {
                return Map.of("enabled", false);
            }
            return null;
        }, mod);
    }
}

package me.melontini.andromeda.modules.blocks.bed.unsafe;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.modules.blocks.bed.safe.Safe;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.config.OptionProcessorRegistry;
import net.fabricmc.loader.api.ModContainer;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
@ModuleTooltip(2)
@ModuleInfo(name = "bed/unsafe", category = "blocks", environment = Environment.SERVER)
public class Unsafe extends BasicModule {

    @Override
    public void onProcessors(OptionProcessorRegistry<BasicConfig> registry, ModContainer mod) {
        registry.register(CommonValues.MODID + ":module_conflict", manager -> {
            if (ModuleManager.get().getModule(Safe.class).map(m->m.config().enabled).orElse(false)) {
                return Map.of("enabled", false);
            }
            return null;
        }, mod);
    }
}

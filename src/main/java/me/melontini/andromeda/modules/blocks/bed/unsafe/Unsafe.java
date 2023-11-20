package me.melontini.andromeda.modules.blocks.bed.unsafe;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.modules.blocks.bed.safe.Safe;
import me.melontini.dark_matter.api.config.ConfigBuilder;

import java.util.Map;

@ModuleTooltip(2)
@FeatureEnvironment(Environment.SERVER)
public class Unsafe implements BasicModule {

    @Override
    public void onConfig(ConfigBuilder<BasicConfig> builder) {
        builder.processors((registry, mod) -> registry.register("andromeda:module_conflict", manager -> {
            if (ModuleManager.get().getModule(Safe.class).map(m->m.config().enabled).orElse(false)) {
                return Map.of("enabled", false);
            }
            return null;
        }, mod));
    }
}

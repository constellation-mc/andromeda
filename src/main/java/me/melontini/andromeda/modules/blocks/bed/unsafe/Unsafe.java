package me.melontini.andromeda.modules.blocks.bed.unsafe;

import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.modules.blocks.bed.safe.Safe;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.config.ConfigBuilder;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
@ModuleTooltip(2)
@ModuleInfo(name = "bed/unsafe", category = "blocks", environment = Environment.SERVER)
public class Unsafe extends BasicModule {

    @Override
    public void onConfig(ConfigBuilder<BasicConfig> builder) {
        builder.processors((registry, mod) ->
                registry.register(CommonValues.MODID + ":module_conflict", manager -> {
                    if (ModuleManager.get().getDiscovered(Safe.class).filter(Module::enabled).isPresent()) {
                        return Map.of("enabled", false);
                    }
                    return null;
                }, mod));
    }

    @Override
    public void postConfig() {
        ModuleManager.get().getDiscovered(Safe.class)
                .ifPresent(m -> m.manager().postSave(manager -> this.manager().save()));
    }
}

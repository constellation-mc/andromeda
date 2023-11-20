package me.melontini.andromeda.modules.items.minecart_block_picking;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.modules.entities.minecarts.Minecarts;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.config.ConfigBuilder;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Map;

@ModuleTooltip
@SuppressWarnings("UnstableApiUsage")
public class MinecartBlockPicking implements Module<MinecartBlockPicking.Config> {

    @Override
    public void onConfig(ConfigBuilder<Config> builder) {
        builder.processors((registry, mod) -> registry.register(CommonValues.MODID + ":spawner_enabled", manager -> {
            if (ModuleManager.get().getModule(Minecarts.class)
                    .map(m->!m.config().isSpawnerMinecartOn).orElse(true)) {
                return Map.of("spawnerPicking", false);
            }
            return null;
        }, mod));
    }

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        public boolean spawnerPicking = false;
    }
}

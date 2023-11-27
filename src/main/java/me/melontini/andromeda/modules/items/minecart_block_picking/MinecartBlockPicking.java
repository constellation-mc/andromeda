package me.melontini.andromeda.modules.items.minecart_block_picking;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.modules.entities.minecarts.Minecarts;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.config.OptionProcessorRegistry;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.loader.api.ModContainer;

import java.util.Map;

@ModuleTooltip
@ModuleInfo(name = "minecart_block_picking", category = "items")
@SuppressWarnings("UnstableApiUsage")
public class MinecartBlockPicking extends Module<MinecartBlockPicking.Config> {

    @Override
    public void onProcessors(OptionProcessorRegistry<Config> registry, ModContainer mod) {
        registry.register(CommonValues.MODID + ":spawner_enabled", manager -> {
            if (ModuleManager.get().getModule(Minecarts.class)
                    .map(m->!m.config().isSpawnerMinecartOn).orElse(true)) {
                return Map.of("spawnerPicking", false);
            }
            return null;
        }, mod);
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

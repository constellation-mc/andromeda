package me.melontini.andromeda.modules.world.auto_planting;

import com.google.common.collect.Lists;
import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import net.minecraft.item.Item;

import java.util.List;

@ModuleInfo(name = "auto_planting", category = "world", environment = Environment.SERVER)
public final class AutoPlanting extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    AutoPlanting() {
        this.defineConfig(ConfigState.GAME, CONFIG);
    }

    @ToString
    public static class Config extends GameConfig {
        public boolean blacklistMode = true;
        public List<Item> idList = Lists.newArrayList();
    }
}

package dev.zenfyr.andromeda.modules.world.auto_planting;

import com.google.common.collect.Lists;
import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.config.GameConfig;
import java.util.List;
import net.minecraft.world.item.Item;

@ModuleInfo(name = "auto_planting", category = "world", env = Environment.SERVER)
public final class AutoPlanting extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  AutoPlanting() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  public static class Config extends GameConfig {
    public boolean blacklistMode = true;
    public List<Item> idList = Lists.newArrayList();
  }
}

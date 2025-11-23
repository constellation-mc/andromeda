package me.melontini.andromeda.modules.world.auto_planting;

import com.google.common.collect.Lists;
import java.util.List;
import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.common.config.GameConfig;
import net.minecraft.world.item.Item;

@ModuleInfo(name = "auto_planting", category = "world")
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

package me.melontini.andromeda.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import lombok.CustomLog;
import me.melontini.dark_matter.api.base.config.ConfigManager;
import me.melontini.dark_matter.api.base.util.Context;
import net.fabricmc.loader.api.FabricLoader;

@CustomLog
public class Experiments {
  private static final ConfigManager<Config> MANAGER = ConfigManager.of(
          Config.class, "andromeda/experiments", Config::new)
      .exceptionHandler((e, stage, path) -> LOGGER.error(
          "Failed to %s experiments config!".formatted(stage.toString().toLowerCase(Locale.ROOT)),
          e));
  private static final Config CONFIG =
      MANAGER.load(FabricLoader.getInstance().getConfigDir(), Context.of());
  private static final Config DEFAULT = MANAGER.createDefault();

  public static Config get() {
    return CONFIG;
  }

  public static Config getDefault() {
    return DEFAULT;
  }

  public static void save() {
    MANAGER.save(FabricLoader.getInstance().getConfigDir(), CONFIG, Context.of());
  }

  public static final class Config {
    public List<String> persistentScopedConfigs = new ArrayList<>();
    public boolean showAvailableOption = false;
    public boolean hideSidedModulesInSideOnly = ThreadLocalRandom.current().nextBoolean();
  }
}

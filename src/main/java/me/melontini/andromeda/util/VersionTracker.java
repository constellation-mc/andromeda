package me.melontini.andromeda.util;

import static me.melontini.andromeda.util.AndromedaConstants.idString;

import lombok.CustomLog;
import me.melontini.andromeda.bootstrap.ModuleManager;
import me.melontini.dark_matter.api.base.util.Exceptions;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;

@CustomLog
public class VersionTracker {

  public static boolean modUpdated() {
    return (boolean) FabricLoader.getInstance().getObjectShare().get(idString("updated"));
  }

  public static void initialize(ModuleManager manager) {
    FabricLoader.getInstance().getObjectShare().put(idString("updated"), checkUpdate(manager));
  }

  private static boolean checkUpdate(ModuleManager manager) {
    Version current = manager.modContainer().getMetadata().getVersion();
    if (manager.dataHolder().hasData("last_version")) {
      Version version = Exceptions.supply(
          () -> Version.parse(manager.dataHolder().getData("last_version").getAsString()));
      if (current.compareTo(version) != 0) {
        log.warn(
            "Andromeda version changed! was [{}], now [{}]",
            version.getFriendlyString(),
            current.getFriendlyString());
        manager
            .dataHolder()
            .putData("last_version", current.getFriendlyString())
            .save();
        return true;
      }
      return false;
    } else {
      manager.dataHolder().putData("last_version", current.getFriendlyString()).save();
      return true;
    }
  }
}

package dev.zenfyr.andromeda.bootstrap.util;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.handler.ModConfigHandler;
import dev.zenfyr.andromeda.util.AndromedaConstants;
import dev.zenfyr.andromeda.util.Util;
import dev.zenfyr.pulsar.api.util.ExceptionUtil;
import java.net.http.HttpClient;
import java.time.Duration;
import lombok.CustomLog;
import net.fabricmc.loader.api.Version;

@CustomLog
public final class NetUtils extends BaseConfig {

  public static final ModConfigHandler.Key<NetUtils> KEY =
      new ModConfigHandler.Key<>("connections", NetUtils.class);

  public static final String OWNER = "constellation-mc";
  public static final String REPO = AndromedaConstants.MODID;
  public static final String RAW_URL = "https://raw.githubusercontent.com";

  private static final HttpClient CLIENT = HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.NORMAL)
      .connectTimeout(Duration.ofSeconds(5))
      .build();

  public boolean allow = true;
  private transient boolean updated = false;

  public HttpClient getClient() {
    if (!allow)
      throw Util.create(
          "HttpClient requested when connections.allow is set to false!",
          IllegalStateException::new);
    return CLIENT;
  }

  public boolean modUpdated() {
    return this.updated;
  }

  public void initialize(ModuleManager manager) {
    this.updated = checkUpdate(manager);
  }

  private static boolean checkUpdate(ModuleManager manager) {
    Version current = manager.modContainer().getMetadata().getVersion();
    if (manager.dataHolder().hasData("last_version")) {
      Version version = ExceptionUtil.supply(
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

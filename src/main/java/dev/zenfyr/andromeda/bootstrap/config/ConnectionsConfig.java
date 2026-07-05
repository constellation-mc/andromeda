package dev.zenfyr.andromeda.bootstrap.config;

import dev.zenfyr.andromeda.bootstrap.AndromedaConstants;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.handler.ModConfigHandler;
import dev.zenfyr.andromeda.bootstrap.util.Util;
import java.net.http.HttpClient;
import java.time.Duration;
import lombok.CustomLog;

@CustomLog
public final class ConnectionsConfig extends BaseConfig {

  public static final ModConfigHandler.Key<ConnectionsConfig> KEY =
      new ModConfigHandler.Key<>("connections", ConnectionsConfig.class);

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
    if (manager.dataHolder().hasData("last_version")) {
      String version = manager.dataHolder().getData("last_version").getAsString();
      if (AndromedaConstants.VERSION.compareTo(version) != 0) {
        log.warn(
            "Andromeda version changed! was '{}', now '{}'", version, AndromedaConstants.VERSION);
        manager.dataHolder().putData("last_version", AndromedaConstants.VERSION).save();
        return true;
      }
      return false;
    } else {
      manager.dataHolder().putData("last_version", AndromedaConstants.VERSION).save();
      return true;
    }
  }
}

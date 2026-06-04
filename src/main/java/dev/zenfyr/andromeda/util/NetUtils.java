package dev.zenfyr.andromeda.util;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.BaseConfig;
import dev.zenfyr.andromeda.bootstrap.config.handler.ModConfigHandler;
import java.net.http.HttpClient;
import java.time.Duration;

public final class NetUtils extends BaseConfig {

  public static final ModConfigHandler.Key<NetUtils> KEY =
      new ModConfigHandler.Key<>("connections", NetUtils.class);

  private static final HttpClient CLIENT = HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.NORMAL)
      .connectTimeout(Duration.ofSeconds(5))
      .build();

  public boolean allow = true;

  public HttpClient getClient() {
    if (!allow)
      throw Util.create(
          "HttpClient requested when connections.allow is set to false!",
          IllegalStateException::new);
    return CLIENT;
  }

  public static NetUtils get() {
    return ModuleManager.get().modConfig().get(KEY);
  }
}

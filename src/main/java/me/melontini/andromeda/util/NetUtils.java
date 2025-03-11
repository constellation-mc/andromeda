package me.melontini.andromeda.util;

import java.net.http.HttpClient;
import java.time.Duration;
import me.melontini.andromeda.bootstrap.ModuleManager;
import me.melontini.andromeda.bootstrap.config.BaseConfig;
import me.melontini.andromeda.bootstrap.config.handler.ModConfigHandler;

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

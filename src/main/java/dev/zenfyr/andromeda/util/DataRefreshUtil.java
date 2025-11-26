package dev.zenfyr.andromeda.util;

import static dev.zenfyr.andromeda.util.AndromedaConstants.idString;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.pulsar.util.ExceptionUtil;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import lombok.CustomLog;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;

@CustomLog
public class DataRefreshUtil {

  private static final Set<String> PRESERVE_KEYS =
      Sets.newHashSet("default_branch", "stargazers_count");

  public static final String OWNER = "constellation-mc";
  public static final String REPO = AndromedaConstants.MODID;

  public static final String RAW_URL = "https://raw.githubusercontent.com";
  public static final String API_URL = "https://api.github.com";

  public static String defaultBranch(ModuleManager manager) {
    JsonObject object = manager.dataHolder().getData("git_tracker").getAsJsonObject();
    if (object.has("default_branch")) return object.get("default_branch").getAsString();
    return "1.20-fabric";
  }

  public static boolean modUpdated() {
    return (boolean) FabricLoader.getInstance().getObjectShare().get(idString("updated"));
  }

  public static void initialize(ModuleManager manager) {
    FabricLoader.getInstance().getObjectShare().put(idString("updated"), checkUpdate(manager));
    if (shouldUpdate(manager)) tryUpdateGitInfo(manager);
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

  private static boolean shouldUpdate(ModuleManager manager) {
    if (!NetUtils.get().allow) return false;
    if (manager.dataHolder().hasData("git_timestamp")) {
      try {
        if (ChronoUnit.HOURS.between(
                DateTimeFormatter.ISO_INSTANT.parse(
                    manager.dataHolder().getData("git_timestamp").getAsString(), Instant::from),
                Instant.now())
            >= 24) return true;
      } catch (Exception ignored) {
        return modUpdated();
      }
    } else return true;
    return modUpdated();
  }

  private static void tryUpdateGitInfo(ModuleManager manager) {
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(API_URL + "/repos/" + OWNER + "/" + REPO))
        .header("Accept", "application/vnd.github+json")
        .build();

    try {
      HttpResponse<String> response =
          NetUtils.get().getClient().send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200)
        throw new RuntimeException(
            "Status Code: " + response.statusCode() + " Body: " + response.body());

      JsonObject jsonResponse = (JsonObject) JsonParser.parseString(response.body());

      Set.copyOf(jsonResponse.keySet()).stream()
          .filter(s -> !PRESERVE_KEYS.contains(s))
          .forEach(jsonResponse::remove);

      manager
          .dataHolder()
          .putData("git_timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
          .putData("git_tracker", jsonResponse)
          .save();
    } catch (Exception e) {
      log.warn("Couldn't update git info", e);
    }
  }
}

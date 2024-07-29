package me.melontini.andromeda.util;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;

@CustomLog
@UtilityClass
public class GitTracker {

  public static final String OWNER = "constellation-mc";
  public static final String REPO = "andromeda";

  public static final String RAW_URL = "https://raw.githubusercontent.com";
  public static final String API_URL = "https://api.github.com";

  private static String DEFAULT_BRANCH = "1.20-fabric";

  private static final Set<String> PRESERVE_KEYS =
      Sets.newHashSet("default_branch", "stargazers_count");

  private static final HttpClient CLIENT = HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.NORMAL)
      .connectTimeout(Duration.ofSeconds(5))
      .build();

  public static String getDefaultBranch() {
    return DEFAULT_BRANCH;
  }

  static {
    var holder = InstanceDataHolder.get();
    if (shouldUpdate(holder)) tryUpdateGitInfo(holder);
    if (holder.hasData("git_tracker")) tryUpdateInfoFromJson(holder);
  }

  public static boolean shouldUpdate(InstanceDataHolder holder) {
    if (Debug.Keys.DISABLE_NETWORK_FEATURES.isPresent()) return false;
    if (holder.hasData("git_timestamp")) {
      try {
        if (ChronoUnit.HOURS.between(
                DateTimeFormatter.ISO_INSTANT.parse(
                    holder.getData("git_timestamp").getAsString(), Instant::from),
                Instant.now())
            >= 24) return true;
      } catch (Exception ignored) {
        return CommonValues.updated();
      }
    } else return true;
    return CommonValues.updated();
  }

  private static void tryUpdateInfoFromJson(InstanceDataHolder holder) {
    JsonObject object = holder.getData("git_tracker").getAsJsonObject();

    if (object.has("default_branch")) {
      DEFAULT_BRANCH = object.get("default_branch").getAsString();
      LOGGER.info("Default branch is: {}", DEFAULT_BRANCH);
    }
  }

  private static void tryUpdateGitInfo(InstanceDataHolder holder) {
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(API_URL + "/repos/" + OWNER + "/" + REPO))
        .header("Accept", "application/vnd.github+json")
        .build();

    try {
      HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200)
        throw new RuntimeException(
            "Status Code: " + response.statusCode() + " Body: " + response.body());

      JsonObject jsonResponse = (JsonObject) JsonParser.parseString(response.body());

      Set.copyOf(jsonResponse.keySet()).stream()
          .filter(s -> !PRESERVE_KEYS.contains(s))
          .forEach(jsonResponse::remove);

      holder.modifyAndSave(() -> holder
          .putData("git_timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
          .putData("git_tracker", jsonResponse));
    } catch (Exception e) {
      LOGGER.warn("Couldn't update git info", e);
    }
  }
}

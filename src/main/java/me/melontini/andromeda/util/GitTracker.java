package me.melontini.andromeda.util;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@CustomLog @UtilityClass
public class GitTracker {

    public static final String OWNER = "melontini";
    public static final String REPO = "andromeda";

    public static final String RAW_URL = "https://raw.githubusercontent.com";
    public static final String API_URL = "https://api.github.com";

    private static String DEFAULT_BRANCH = "1.20-fabric";

    private static final Set<String> PRESERVE_KEYS = Sets.newHashSet("default_branch", "stargazers_count");

    private static final HttpClient CLIENT = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(5)).build();

    public static String getDefaultBranch() {
        return DEFAULT_BRANCH;
    }

    static {
        Path lastResponse = CommonValues.hiddenPath().resolve("git-response.json");
        if (shouldUpdate(lastResponse)) tryUpdateGitInfo(lastResponse);
        if (Files.exists(lastResponse)) tryUpdateInfoFromJson(lastResponse);
    }

    public static boolean shouldUpdate(Path lastResponse) {
        if (Debug.Keys.DISABLE_NETWORK_FEATURES.isPresent()) return false;
        if (Files.exists(lastResponse)) {
            try {
                if (ChronoUnit.HOURS.between(Files.getLastModifiedTime(lastResponse).toInstant(), Instant.now()) >= 24)
                    return true;
            } catch (Exception ignored) {
                return CommonValues.updated();
            }
        } else return true;
        return CommonValues.updated();
    }

    private static void tryUpdateInfoFromJson(Path lastResponse) {
        try {
            JsonObject object = (JsonObject) JsonParser.parseString(Files.readString(lastResponse));

            if (object.has("default_branch")) {
                DEFAULT_BRANCH = object.get("default_branch").getAsString();
                LOGGER.info("Default branch is: {}", DEFAULT_BRANCH);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to update info from JSON!", e);
        }
    }

    private static void tryUpdateGitInfo(Path lastResponse) {
        HttpRequest request = HttpRequest.newBuilder().GET()
                .uri(URI.create(API_URL + "/repos/" + OWNER + "/" + REPO))
                .header("Accept", "application/vnd.github+json")
                .build();

        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) throw new RuntimeException("Status Code: " + response.statusCode() + " Body: " + response.body());

            JsonObject jsonResponse = (JsonObject) JsonParser.parseString(response.body());

            Set.copyOf(jsonResponse.keySet()).stream().filter(s -> !PRESERVE_KEYS.contains(s)).forEach(jsonResponse::remove);

            var parent = lastResponse.getParent();
            if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);
            Files.writeString(lastResponse, jsonResponse.toString());
        } catch (Exception e) {
            LOGGER.warn("Couldn't update git info", e);
        }
    }
}

package me.melontini.andromeda.util;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.experimental.ExtensionMethod;

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
import java.util.HashSet;
import java.util.Set;

@ExtensionMethod(Files.class)
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
        if (lastResponse.exists()) tryUpdateInfoFromJson(lastResponse);
    }

    public static boolean shouldUpdate(Path lastResponse) {
        if (lastResponse.exists()) {
            try {
                if (ChronoUnit.HOURS.between(lastResponse.getLastModifiedTime().toInstant(), Instant.now()) >= 24)
                    return true;
            } catch (Exception ignored) {
            }
        } else return true;
        return CommonValues.updated();
    }

    private static void tryUpdateInfoFromJson(Path lastResponse) {
        try {
            JsonObject object = (JsonObject) JsonParser.parseString(lastResponse.readString());

            if (object.has("default_branch")) {
                DEFAULT_BRANCH = object.get("default_branch").getAsString();
                AndromedaLog.info("Default branch is: {}", DEFAULT_BRANCH);
            }
        } catch (IOException ignored) {}
    }

    private static void tryUpdateGitInfo(Path lastResponse) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/repos/" + OWNER + "/" + REPO))
                .GET()
                .header("Accept", "application/vnd.github+json")
                .build();

        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) throw new RuntimeException("Status Code: " + response.statusCode() + " Body: " + response.body());

            JsonObject jsonResponse = (JsonObject) JsonParser.parseString(response.body());

            for (String s : new HashSet<>(jsonResponse.keySet())) {
                if (!PRESERVE_KEYS.contains(s)) jsonResponse.remove(s);
            }

            if (!lastResponse.exists()) lastResponse.getParent().createDirectories();
            lastResponse.writeString(jsonResponse.toString());
        } catch (Exception e) {
            AndromedaLog.warn("Couldn't update git info", e);
        }
    }
}

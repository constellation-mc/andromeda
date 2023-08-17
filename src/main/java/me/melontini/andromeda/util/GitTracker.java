package me.melontini.andromeda.util;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class GitTracker {

    public static final String OWNER = "melontini";
    public static final String REPO = "andromeda";
    public static final String RAW_URL = "https://raw.githubusercontent.com";
    public static final String API_URL = "https://api.github.com";
    private static String DEFAULT_BRANCH = "1.19-fabric";
    private static final Set<String> PRESERVE_KEYS = Sets.newHashSet("default_branch", "stargazers_count");

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static String getDefaultBranch() {
        return DEFAULT_BRANCH;
    }

    static {
        Path lastResponse = SharedConstants.HIDDEN_PATH.resolve("git-response.json");

        boolean shouldUpdate = true;
        if (Files.exists(lastResponse)) {
            try {
                FileTime lastModifiedTime = Files.getLastModifiedTime(lastResponse);
                shouldUpdate = ChronoUnit.HOURS.between(lastModifiedTime.toInstant(), Instant.now()) >= 24;
            } catch (Exception ignored) {
            }
        }
        if (!shouldUpdate) shouldUpdate = SharedConstants.MOD_UPDATED;

        if (shouldUpdate) {
            tryUpdateGitInfo(lastResponse);
        } else AndromedaLog.info("Skipped git info update.");

        if (Files.exists(lastResponse)) {
            tryUpdateInfoFromJson(lastResponse);
        }
    }

    private static void tryUpdateInfoFromJson(Path lastResponse) {
        try {
            JsonObject object = (JsonObject) JsonParser.parseString(Files.readString(lastResponse));

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

            if (!Files.exists(lastResponse)) Files.createDirectories(lastResponse.getParent());
            Files.writeString(lastResponse, jsonResponse.toString());
        } catch (Exception e) {
            AndromedaLog.warn("Couldn't update git info", e);
        }
    }

}

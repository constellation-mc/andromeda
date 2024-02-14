package me.melontini.andromeda.modules.misc.translations.client;

import com.google.common.collect.Sets;
import lombok.experimental.ExtensionMethod;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.GitTracker;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

@ExtensionMethod(Files.class)
public class Client {

    public static final Path TRANSLATION_PACK = CommonValues.hiddenPath().resolve("andromeda_translations");
    public static final Path LANG_PATH = TRANSLATION_PACK.resolve("assets/andromeda/lang");
    private static final Path EN_US = LANG_PATH.resolve("en_us.json");
    private static final Path OPTIONS = FabricLoader.getInstance().getGameDir().resolve("options.txt");

    private static final String URL = GitTracker.RAW_URL + "/" + GitTracker.OWNER + "/" + GitTracker.REPO + "/" + GitTracker.getDefaultBranch() + "/src/main/resources/assets/andromeda/lang/";
    private static final HttpClient CLIENT = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

    private static String languageCode = "en_us";

    Client() {
        if (shouldUpdate()) {
            Set<String> languages = Sets.newHashSet("en_us");
            Client.getSelectedLanguage().ifPresent(languages::add);
            ForkJoinPool.commonPool().submit(() -> Client.downloadTranslations(languages));
        }
    }

    public boolean shouldUpdate() {
        if (EN_US.exists()) {
            try {
                if (ChronoUnit.HOURS.between(EN_US.getLastModifiedTime().toInstant(), Instant.now()) >= 24)
                    return true;
            } catch (Exception ignored) {
            }
        } else return true;
        return CommonValues.updated();
    }

    public static void onResourceReload(String code) {
        if (!languageCode.equals(code)) {
            languageCode = code;
            Set<String> languages = Sets.newHashSet("en_us");
            languages.add(code);
            downloadTranslations(languages);
        }
    }

    public static void downloadTranslations(Set<String> languages) {
        for (String language : languages) {
            String file = downloadLang(language);
            if (!file.isEmpty()) {
                try {
                    if (!LANG_PATH.exists()) LANG_PATH.createDirectories();
                    LANG_PATH.resolve(language + ".json").writeString(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static String downloadLang(String language) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL + language + ".json"))
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                AndromedaLog.info("Couldn't download " + language + ".json" + ". Status code: " + response.statusCode() + " Body: " + response.body());
                return "";
            }

            AndromedaLog.info("Downloaded " + language + ".json");
            return response.body();
        } catch (IOException | InterruptedException e) {
            AndromedaLog.error("Couldn't download " + language + ".json", e);
            return "";
        }
    }

    public static Optional<String> getSelectedLanguage() {
        try {
            if (!OPTIONS.exists()) return Optional.empty();
            for (String line : OPTIONS.readAllLines()) {
                if (line.matches("^lang:\\w+_\\w+")) {
                    return Optional.of(line.replace("lang:", ""));
                }
            }
            throw AndromedaException.builder()
                    .report(false).message("Mo valid language option found!")
                    .build();
        } catch (Throwable e) {
            AndromedaLog.error("Couldn't determine selected language!", e);
            return Optional.empty();
        }
    }
}

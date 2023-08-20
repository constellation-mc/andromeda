package me.melontini.andromeda.util.translations;

import com.google.common.collect.Sets;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.GitTracker;
import me.melontini.andromeda.util.SharedConstants;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class TranslationUpdater {
    public static final Path TRANSLATION_PACK = SharedConstants.HIDDEN_PATH.resolve("andromeda_translations");
    public static final Path LANG_PATH = TRANSLATION_PACK.resolve("assets/andromeda/lang");
    private static final Path OPTIONS = FabricLoader.getInstance().getGameDir().resolve("options.txt");
    private static final String URL = GitTracker.RAW_URL + "/" + GitTracker.OWNER + "/" + GitTracker.REPO + "/" + GitTracker.getDefaultBranch() + "/src/main/resources/assets/andromeda/lang/";
    private static final HttpClient CLIENT = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private static String languageCode = "en_us";

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
                    if (!Files.exists(LANG_PATH)) Files.createDirectories(LANG_PATH);
                    Files.writeString(LANG_PATH.resolve(language + ".json"), file);
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

    public static String getSelectedLanguage() {
        try {
            if (!Files.exists(OPTIONS)) return "";
            for (String line : Files.readAllLines(OPTIONS)) {
                if (line.matches("^lang:\\w+_\\w+")) {
                    languageCode = line.replace("lang:", "");
                    return languageCode;
                }
            }
            throw new AndromedaException(false, "Invalid language option!");
        } catch (Throwable e) {
            AndromedaLog.error("Couldn't determine selected language!", e);
            return "";
        }
    }
}

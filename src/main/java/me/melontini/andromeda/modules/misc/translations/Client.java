package me.melontini.andromeda.modules.misc.translations;

import com.google.common.collect.Sets;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.andromeda.util.EarlyLanguage;
import me.melontini.andromeda.util.GitTracker;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Environment(EnvType.CLIENT)
public final class Client {

    private static final String URL = GitTracker.RAW_URL + "/" + GitTracker.OWNER + "/" + GitTracker.REPO + "/" + GitTracker.getDefaultBranch() + "/src/main/resources/assets/andromeda/lang/";
    private static final HttpClient CLIENT = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

    private static String languageCode = "en_us";

    static void init(Translations module) {
        if (shouldUpdate()) {
            Set<String> languages = Sets.newHashSet("en_us");
            Client.getSelectedLanguage(module).ifPresent(languages::add);
            CompletableFuture.runAsync(() -> Client.downloadTranslations(languages, module), ForkJoinPool.commonPool()).handle((unused, throwable) -> {
                if (throwable != null) module.logger().error("Failed to download translations!", throwable);
                return null;
            });
        }
    }

    public static boolean shouldUpdate() {
        if (Debug.Keys.DISABLE_NETWORK_FEATURES.isPresent()) return false;
        if (Files.exists(Translations.EN_US)) {
            try {
                if (ChronoUnit.HOURS.between(Files.getLastModifiedTime(Translations.EN_US).toInstant(), Instant.now()) >= 24)
                    return true;
            } catch (Exception ignored) {
                return CommonValues.updated();
            }
        } else return true;
        return CommonValues.updated();
    }

    public static void onResourceReload(String code, Translations module) {
        if (!languageCode.equals(code)) {
            languageCode = code;
            Set<String> languages = Sets.newHashSet("en_us");
            languages.add(code);
            downloadTranslations(languages, module);
        }
    }

    public static void downloadTranslations(Set<String> languages, Translations module) {
        for (String language : languages) {
            String file = downloadLang(language, module);
            if (!file.isEmpty()) {
                try {
                    if (!Files.exists(Translations.LANG_PATH)) Files.createDirectories(Translations.LANG_PATH);
                    Files.writeString(Translations.LANG_PATH.resolve(language + ".json"), file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static String downloadLang(String language, Translations module) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL + language + ".json"))
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                module.logger().info("Couldn't download " + language + ".json" + ". Status code: " + response.statusCode() + " Body: " + response.body());
                return "";
            }

            module.logger().info("Downloaded " + language + ".json");
            return response.body();
        } catch (IOException | InterruptedException e) {
            module.logger().error("Couldn't download " + language + ".json", e);
            return "";
        }
    }

    public static Optional<String> getSelectedLanguage(Translations module) {
        try {
            if (!Files.exists(Translations.OPTIONS)) return Optional.empty();
            for (String line : Files.readAllLines(Translations.OPTIONS)) {
                if (line.matches("^lang:\\w+_\\w+")) {
                    return Optional.of(line.replace("lang:", ""));
                }
            }
            throw AndromedaException.builder()
                    .report(false).translatable(module, "no_valid_lang")
                    .build();
        } catch (Throwable e) {
            module.logger().error(EarlyLanguage.translate(module, "failed_lang_acquire"), e);
            return Optional.empty();
        }
    }
}

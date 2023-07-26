package me.melontini.andromeda.util.translations;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.util.Utilities;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class AndromedaTranslations {
    public static final Path TRANSLATION_PACK = FabricLoader.getInstance().getGameDir().resolve(".andromeda/andromeda_translations");
    private static final Path LANG_PATH = TRANSLATION_PACK.resolve("assets/andromeda/lang");
    private static final Path OPTIONS = FabricLoader.getInstance().getGameDir().resolve("options.txt");
    private static final String URL = "https://raw.githubusercontent.com/melontini/andromeda/1.19-fabric/src/main/resources/assets/andromeda/lang/";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
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
        if (!AutoConfig.getConfigHolder(AndromedaConfig.class).getConfig().autoUpdateTranslations) return;
        for (String language : languages) {
            Path langPath = LANG_PATH.resolve(language + ".json");

            if (!Files.exists(langPath)) {
                if (copyLocalLangFile(language)) {
                    String file = downloadLang(language);
                    if (!file.isEmpty()) {
                        mergeAndSave(language, file);
                    }
                } else {
                    String file = downloadLang(language);
                    if (!file.isEmpty()) {
                        try {
                            Files.writeString(langPath, file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } else {
                String file = downloadLang(language);
                if (!file.isEmpty()) {
                    mergeAndSave(language, file);
                }
            }
        }
    }

    private static void mergeAndSave(String language, String lang) {
        try {
            Path langPath = LANG_PATH.resolve(language + ".json");
            JsonObject newLangFile = JsonParser.parseString(lang).getAsJsonObject();
            JsonObject oldLangFile = Utilities.supply(() -> {
                try {
                    return JsonParser.parseReader(Files.newBufferedReader(langPath)).getAsJsonObject();
                } catch (Exception e) {
                    try {
                        copyLocalLangFile(language);
                        return JsonParser.parseReader(Files.newBufferedReader(langPath)).getAsJsonObject();
                    } catch (Exception e1) {
                        return new JsonObject();
                    }
                }
            });

            if (!newLangFile.equals(oldLangFile)) {
                for (Map.Entry<String, JsonElement> entry : oldLangFile.entrySet()) {
                    if (!newLangFile.has(entry.getKey())) newLangFile.add(entry.getKey(), entry.getValue());
                }

                Files.writeString(langPath, newLangFile.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    private static boolean copyLocalLangFile(String language) {
        try (InputStream stream = AndromedaTranslations.class.getClassLoader().getResourceAsStream("assets/andromeda/lang/" + language + ".json")) {
            if (stream != null) {
                if (!Files.exists(LANG_PATH)) Files.createDirectories(LANG_PATH);
                Files.write(LANG_PATH.resolve(language + ".json"), stream.readAllBytes());
                AndromedaLog.info("Copied local " + language + ".json");
                return true;
            } else throw new NullPointerException();
        } catch (Throwable e) {
            AndromedaLog.error("No local copy of " + language + ".json");
            return false;
        }
    }

    public static String getSelectedLanguage() {
        try {
            for (String line : Files.readAllLines(OPTIONS)) {
                if (line.matches("^lang:\\w+_\\w+")) {
                    languageCode = line.replace("lang:", "");
                    return line.replace("lang:", "");
                }
            }
            throw new AndromedaException(false, "Invalid language option!");
        } catch (Throwable e) {
            AndromedaLog.error("Couldn't determine selected language!", e);
            return "";
        }
    }
}

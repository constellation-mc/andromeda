package dev.zenfyr.andromeda.modules.misc.translations;

import com.google.common.collect.Sets;
import dev.zenfyr.andromeda.bootstrap.AndromedaConstants;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.util.Util;
import dev.zenfyr.andromeda.common.client.AndromedaClient;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public final class TranslationsClient {

  public static final String RAW_URL = "https://raw.githubusercontent.com";
  public static final String REPO = AndromedaConstants.MODID;
  public static final String OWNER = "constellation-mc";
  private static final String URL = RAW_URL + "/" + OWNER + "/" + REPO + "/";

  private static String languageCode = "en_us";

  static void init() {
    var manager = ModuleManager.get();
    var module = manager.get(Translations.class).orElseThrow();

    if (shouldUpdate(manager)) {
      Set<String> languages = Sets.newHashSet("en_us");
      TranslationsClient.getSelectedLanguage(module).ifPresent(languages::add);
      CompletableFuture.runAsync(
              () -> TranslationsClient.downloadTranslations(languages, manager, module),
              ForkJoinPool.commonPool())
          .handle((unused, throwable) -> {
            if (throwable != null)
              module.logger().error("Failed to download translations!", throwable);
            return null;
          });
    }
  }

  static boolean shouldUpdate(ModuleManager manager) {
    if (!manager.connections().allow) return false;
    if (Files.exists(Translations.EN_US)) {
      try {
        if (ChronoUnit.HOURS.between(
                Files.getLastModifiedTime(Translations.EN_US).toInstant(), Instant.now())
            >= 24) return true;
      } catch (Exception ignored) {
        return manager.connections().modUpdated();
      }
    } else return true;
    return manager.connections().modUpdated();
  }

  public static void onResourceReload(String code, ModuleManager manager) {
    if (manager.connections().allow && !languageCode.equals(code)) {
      languageCode = code;
      Set<String> languages = Sets.newHashSet("en_us");
      languages.add(code);
      downloadTranslations(languages, manager, manager.get(Translations.class).orElseThrow());
    }
  }

  public static void downloadTranslations(
      Set<String> languages, ModuleManager manager, Translations module) {
    for (String language : languages) {
      String file = downloadLang(language, manager, module);
      if (!file.isEmpty()) {
        try {
          if (!Files.exists(Translations.LANG_PATH))
            Files.createDirectories(Translations.LANG_PATH);
          Files.writeString(Translations.LANG_PATH.resolve(language + ".json"), file);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private static String normalizeBaseUrl(String baseUrl) {
    return baseUrl.endsWith("/") ? baseUrl : baseUrl + '/';
  }

  private static String downloadLang(String language, ModuleManager manager, Translations module) {
    try {
      var config = AndromedaClient.CLIENT.get(Translations.CONFIG);
      String baseUrl = normalizeBaseUrl(
          "default".equals(config.baseUrl)
              ? URL + Translations.BRANCH + "/src/main/resources/assets/andromeda/lang/"
              : config.baseUrl);

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + language + ".json"))
          .GET()
          .build();

      HttpResponse<String> response =
          manager.connections().getClient().send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        module
            .logger()
            .info(
                "Couldn't download {}.json. Status code: {} Body: {}",
                language,
                response.statusCode(),
                response.body());
        return "";
      }

      module.logger().info("Downloaded {}.json", language);
      return response.body();
    } catch (IOException | InterruptedException e) {
      module.logger().error("Couldn't download {}.json", language, e);
      return "";
    }
  }

  public static Optional<String> getSelectedLanguage(Translations module) {
    try {
      if (!Files.exists(Translations.OPTIONS)) return Optional.empty();
      for (String line : Files.readAllLines(Translations.OPTIONS))
        if (line.matches("^lang:\\w+_\\w+")) return Optional.of(line.replace("lang:", ""));
      throw Util.create("No valid language option found!");
    } catch (Throwable e) {
      module.logger().error("Couldn't determine selected language!", e);
      return Optional.empty();
    }
  }
}

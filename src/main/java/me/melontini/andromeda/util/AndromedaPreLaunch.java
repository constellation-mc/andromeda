package me.melontini.andromeda.util;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.util.translations.AndromedaTranslations;
import me.melontini.dark_matter.analytics.MessageHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class AndromedaPreLaunch implements PreLaunchEntrypoint {
    public static AndromedaConfig preLaunchConfig;
    @Override
    public void onPreLaunch() {
        MixinExtrasBootstrap.init();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Set<String> languages = Sets.newHashSet("en_us");
            String s = AndromedaTranslations.getSelectedLanguage();
            if (!s.isEmpty()) languages.add(s);
            MessageHandler.EXECUTOR.submit(() -> AndromedaTranslations.downloadTranslations(languages));
        }
    }

    static {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path config = FabricLoader.getInstance().getConfigDir().resolve("andromeda.json");
        if (Files.exists(config)) {
            try {
                preLaunchConfig = gson.fromJson(Files.readString(config), AndromedaConfig.class);
                Files.write(config, gson.toJson(preLaunchConfig).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            preLaunchConfig = new AndromedaConfig();
            try {
                Files.createFile(config);
                Files.write(config, gson.toJson(preLaunchConfig).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

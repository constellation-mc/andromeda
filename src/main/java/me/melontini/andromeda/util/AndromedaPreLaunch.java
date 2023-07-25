package me.melontini.andromeda.util;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import me.melontini.andromeda.util.translations.AndromedaTranslations;
import me.melontini.dark_matter.analytics.MessageHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.util.Set;

public class AndromedaPreLaunch implements PreLaunchEntrypoint {
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
}

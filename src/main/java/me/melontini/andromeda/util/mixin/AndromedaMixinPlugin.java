package me.melontini.andromeda.util.mixin;

import lombok.CustomLog;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("UnstableApiUsage")
@CustomLog
public class AndromedaMixinPlugin extends ExtendablePlugin {

    @Override
    public void onPluginLoad(String mixinPackage) {
        LOGGER.info("Andromeda({}) on {}({})", CommonValues.version(), CommonValues.platform(), CommonValues.platform().version());

        try {
            FrameworkPatch.patch();
        } catch (Throwable e) {
            LOGGER.error("Failed to patch the mixin framework!", e);
        }

        Mixins.registerErrorHandlerClass(ErrorHandler.class.getName());

        Path newCfg = FabricLoader.getInstance().getConfigDir().resolve("andromeda.json");
        if (Files.exists(newCfg) && !Files.exists(CommonValues.configPath())) {
            try {
                Files.createDirectories(CommonValues.configPath().getParent());
                Files.move(newCfg, CommonValues.configPath());
            } catch (IOException e) {
                AndromedaLog.error("Couldn't rename old m-tweaks config!", e);
            }
        }

        if (this.isDev()) LOGGER.warn("Will be verifying mixins!");
    }
}

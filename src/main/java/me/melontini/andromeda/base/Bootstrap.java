package me.melontini.andromeda.base;

import com.google.common.reflect.ClassPath;
import lombok.CustomLog;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.client.AndromedaClient;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.mixin.ErrorHandler;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("UnstableApiUsage")
@CustomLog
public class Bootstrap {

    public static ModuleManager INSTANCE;
    private static final ClassPath CLASS_PATH = Utilities.supplyUnchecked(() -> ClassPath.from(MixinProcessor.class.getClassLoader()));

    @Environment(EnvType.CLIENT)
    public static void onClient() {
        for (Module<?> module : ModuleManager.get().loaded()) { module.onClient(); }
        AndromedaClient.init();
    }

    @Environment(EnvType.SERVER)
    public static void onServer() {
        for (Module<?> module : ModuleManager.get().loaded()) { module.onServer(); }
    }

    public static void onMain() {
        if (Mixins.getUnvisitedCount() > 0) {
            for (org.spongepowered.asm.mixin.transformer.Config config : Mixins.getConfigs()) {
                if (!config.isVisited() && config.getName().startsWith("andromeda_dynamic$$"))
                    throw new IllegalStateException("Mixin failed to consume Andromeda's late configs!");
            }
        }

        for (Module<?> module : ModuleManager.get().loaded()) { module.onMain(); }
        Andromeda.init();
    }

    public static void onPreLaunch() {
        Config.get();

        ModuleManager m = (INSTANCE = new ModuleManager());

        m.prepare();
        m.print();

        MixinProcessor.addMixins(m);
        for (Module<?> module : m.loaded()) { module.onPreLaunch(); }
    }

    public static void onPluginLoad() {
        LOGGER.info("Andromeda({}) on {}({})", CommonValues.version(), CommonValues.platform(), CommonValues.platform().version());

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

        if (FabricLoader.getInstance().isDevelopmentEnvironment())
            LOGGER.warn("Will be verifying mixins!");
    }

    public static ClassPath getKnotClassPath() {
        return CLASS_PATH;
    }
}

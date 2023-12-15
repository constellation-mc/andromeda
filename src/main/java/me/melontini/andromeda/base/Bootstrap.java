package me.melontini.andromeda.base;

import lombok.CustomLog;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.client.AndromedaClient;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.ClassPath;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.spongepowered.asm.mixin.Mixins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@CustomLog
public class Bootstrap {

    static ModuleManager INSTANCE;
    private static final ClassPath CLASS_PATH = Utilities.supplyUnchecked(ClassPath::from);

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
        LOGGER.info("Andromeda({}) on {}({})", CommonValues.version(), CommonValues.platform(), CommonValues.platform().version());

        if (CommonValues.platform() == CommonValues.Platform.CONNECTOR) {
            LOGGER.warn("Andromeda may not work on Connector! (If #557 is open on Connector's GitHub)");
        }

        Path newCfg = FabricLoader.getInstance().getConfigDir().resolve("andromeda.json");
        if (Files.exists(newCfg) && !Files.exists(CommonValues.configPath())) {
            try {
                Files.createDirectories(CommonValues.configPath().getParent());
                Files.move(newCfg, CommonValues.configPath());
            } catch (IOException e) {
                AndromedaLog.error("Couldn't rename old m-tweaks config!", e);
            }
        }

        Config.get();

        List<Module<?>> list = new ArrayList<>(Arrays.asList(ServiceLoader.load(Module.class)
                .stream().map(ServiceLoader.Provider::get).toArray(Module<?>[]::new)));

        EntrypointRunner.run("andromeda:modules", ModuleManager.ModuleSupplier.class, s -> list.addAll(s.get()));

        if (list.isEmpty()) {
            LOGGER.error("Andromeda couldn't discover any modules! This should not happen!");
        }

        list.removeIf(m -> (m.meta().environment() == me.melontini.andromeda.base.Environment.CLIENT && CommonValues.environment() == EnvType.SERVER));

        ModuleManager m;
        try {
            m = new ModuleManager(list);
        } catch (Throwable t) {
            throw new AndromedaException("Failed to initialize ModuleManager!!!", t);
        }
        m.print();
        //Scan for mixins.
        m.loaded().forEach(module -> getModuleClassPath().addUrl(module.getClass().getProtectionDomain().getCodeSource().getLocation()));
        MixinProcessor.addMixins(m);
        FabricLoader.getInstance().getObjectShare().put("andromeda:module_manager", m);

        for (Module<?> module : m.loaded()) { module.onPreLaunch(); }
    }

    public static ClassPath getModuleClassPath() {
        return CLASS_PATH;
    }

    public static boolean testModVersion(String modId, String predicate) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
        if (mod.isPresent()) {
            try {
                VersionPredicate version = VersionPredicate.parse(predicate);
                return version.test(mod.get().getMetadata().getVersion());
            } catch (VersionParsingException e) {
                return false;
            }
        }
        return false;
    }
}

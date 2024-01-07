package me.melontini.andromeda.base;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.CustomLog;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.client.AndromedaClient;
import me.melontini.andromeda.util.ClassPath;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.CrashHandler;
import me.melontini.andromeda.util.Debug;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.andromeda.util.mixin.AndromedaMixins;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.base.util.classes.ThrowingRunnable;
import me.melontini.dark_matter.api.crash_handler.Crashlytics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static me.melontini.andromeda.util.exceptions.AndromedaException.run;

/**
 * Bootstrap is responsible for bootstrapping the bulk of Andromeda.
 * <p> This includes, but not limited to: <br/>
 * <ul>
 *     <li>Discovering modules.</li>
 *     <li>Constructing the {@link ModuleManager}.</li>
 *     <li>Injecting mixin configs. {@link MixinProcessor}</li>
 *     <li>Running module entrypoints.</li>
 *     <li>Performing basic module verification.</li>
 * </ul>
 */
@CustomLog
public class Bootstrap {

    static ModuleManager INSTANCE;

    @Environment(EnvType.CLIENT)
    public static void onClient() {
        Status.update(Status.CLIENT);

        if (Debug.hasKey(Debug.Keys.VERIFY_MIXINS))
            MixinEnvironment.getCurrentEnvironment().audit();

        for (Module<?> module : ModuleManager.get().loaded()) {
            run(module::onClient, (b) -> b.message("Failed to execute Module.onClient!").add("module", module.meta().id()));
        }
        run(AndromedaClient::init, b -> b.message("Failed to initialize AndromedaClient!"));
    }

    @Environment(EnvType.SERVER)
    public static void onServer() {
        Status.update(Status.SERVER);

        if (Debug.hasKey(Debug.Keys.VERIFY_MIXINS))
            MixinEnvironment.getCurrentEnvironment().audit();

        for (Module<?> module : ModuleManager.get().loaded()) {
            run(module::onServer, (b) -> b.message("Failed to execute Module.onServer!").add("module", module.meta().id()));
        }
    }

    public static void onMain() {
        Status.update(Status.MAIN);
        if (Mixins.getUnvisitedCount() > 0) {
            for (org.spongepowered.asm.mixin.transformer.Config config : Mixins.getConfigs()) {
                if (!config.isVisited() && config.getName().startsWith("andromeda_dynamic$$"))
                    throw AndromedaException.builder()
                            .message("Mixin failed to consume Andromeda's late configs!").message(MixinProcessor.NOTICE)
                            .add("mixin_config", config.getName())
                            .build();
            }
        }

        for (Module<?> module : ModuleManager.get().loaded()) {
            run(module::onMain, (b) -> b.message("Failed to execute Module.onMain!").add("module", module.meta().id()));
        }

        run(Andromeda::init, b -> b.message("Failed to initialize Andromeda!"));
    }

    public static void onPreLaunch() {
        LOGGER.info("Andromeda({}) on {}({})", CommonValues.version(), CommonValues.platform(), CommonValues.platform().version());

        AtomicReference<JsonObject> oldCfg = new AtomicReference<>();
        var oldCfgPath = FabricLoader.getInstance().getConfigDir().resolve("andromeda.json");
        if (Files.exists(oldCfgPath)) {
            if (!Files.exists(CommonValues.configPath())) {
                wrapIO(() -> {
                    oldCfg.set(JsonParser.parseReader(Files.newBufferedReader(oldCfgPath)).getAsJsonObject());
                    Files.createDirectories(CommonValues.configPath().getParent());
                    Files.move(oldCfgPath, CommonValues.configPath());
                }, "Couldn't rename pre-1.0.0 config!");
            } else {
                wrapIO(() -> Files.delete(oldCfgPath), "Couldn't delete pre-1.0.0 config!");
            }
        }

        AndromedaConfig.save();

        Status.update(Status.DISCOVERY);

        List<Module<?>> list = new ArrayList<>(40);
        run(() -> {
            //This should probably be removed.
            ServiceLoader.load(Module.class).stream().map(ServiceLoader.Provider::get).forEach(list::add);
            EntrypointRunner.run("andromeda:modules", ModuleManager.ModuleSupplier.class, s -> list.addAll(s.get()));
        }, (b) -> b.message("Failed during module discovery!"));

        if (list.isEmpty()) {
            LOGGER.error("Andromeda couldn't discover any modules! This should not happen!");
        }

        list.removeIf(m -> (m.meta().environment() == me.melontini.andromeda.base.Environment.CLIENT && CommonValues.environment() == EnvType.SERVER));

        resolveConflicts(list);

        List<Module<?>> sorted = list.stream().sorted(Comparator.comparingInt(m -> {
            int i = ModuleManager.CATEGORIES.indexOf(m.meta().category());
            return i >= 0 ? i : ModuleManager.CATEGORIES.size();
        })).toList();

        Status.update(Status.SETUP);

        ModuleManager m;
        try {
            m = new ModuleManager(sorted, oldCfg.get());
        } catch (Throwable t) {//Manager constructor does a lot of heavy-lifting, so we want to catch any errors.
            throw AndromedaException.builder()
                    .cause(t).message("Failed to initialize ModuleManager!!!")
                    .build();
        }
        m.print();
        //Scan for mixins.
        m.loaded().forEach(module -> getModuleClassPath().addUrl(module.getClass().getProtectionDomain().getCodeSource().getLocation()));
        run(() -> MixinProcessor.addMixins(m), (b) -> b.message("Failed to inject dynamic mixin configs!").message(MixinProcessor.NOTICE));
        FabricLoader.getInstance().getObjectShare().put("andromeda:module_manager", m);

        Status.update(Status.PRE_LAUNCH);
        Crashlytics.addHandler("andromeda", CrashHandler::handleCrash);

        for (Module<?> module : ModuleManager.get().loaded()) {
            run(module::onPreLaunch, (b) -> b.message("Failed to execute Module.onPreLaunch!").add("module", module.meta().id()));
        }
    }

    private static void resolveConflicts(Collection<Module<?>> list) {
        Map<String, Module<?>> packages = new HashMap<>();
        Map<String, Module<?>> ids = new HashMap<>();
        for (Module<?> module : list) {
            ModuleManager.validateModule(module);

            var id = ids.put(module.meta().id(), module);
            if (id != null)
                throw AndromedaException.builder()
                        .message("Duplicate module IDs!")
                        .add("identifier", module.meta().id()).add("module", id.getClass()).add("duplicate", module.getClass())
                        .build();

            var pkg = packages.put(module.getClass().getPackageName(), module);
            if (pkg != null)
                throw AndromedaException.builder()
                        .message("Duplicate module packages!")
                        .add("package", module.getClass().getPackageName()).add("module", pkg.getClass()).add("duplicate", module.getClass())
                        .build();
        }
    }

    static void wrapIO(ThrowingRunnable<IOException> runnable, String msg) {
        try {
            runnable.run();
        } catch (IOException e) {
            LOGGER.error(msg, e);
        }
    }

    public static ClassPath getModuleClassPath() {
        return AndromedaMixins.getClassPath();
    }

    public static boolean testModVersion(Module<?> m, String modId, String predicate) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
        if (mod.isPresent() && !Debug.skipIntegration(m.meta().id(), modId)) {
            try {
                VersionPredicate version = VersionPredicate.parse(predicate);
                return version.test(mod.get().getMetadata().getVersion());
            } catch (VersionParsingException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isModLoaded(Module<?> m, String modId) {
        return !Debug.skipIntegration(m.meta().id(), modId) && FabricLoader.getInstance().isModLoaded(modId);
    }

    public enum Status {
        PRE_INIT, DISCOVERY, SETUP,
        PRE_LAUNCH, MAIN, CLIENT, SERVER;

        private static volatile Status CURRENT = PRE_INIT;

        public static void update(Status status) {
            Status.CURRENT = status;
            LOGGER.debug("Status updated to {}", status);
        }

        public static Status get() {
            return CURRENT;
        }
    }
}

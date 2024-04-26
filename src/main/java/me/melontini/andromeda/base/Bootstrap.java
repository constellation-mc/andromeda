package me.melontini.andromeda.base;

import com.google.common.collect.ImmutableMap;
import lombok.CustomLog;
import me.melontini.andromeda.base.events.Bus;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Experiments;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.client.AndromedaClient;
import me.melontini.andromeda.util.*;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.andromeda.util.mixins.AndromedaMixins;
import me.melontini.dark_matter.api.base.util.Context;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.base.util.functions.ThrowingRunnable;
import me.melontini.dark_matter.api.crash_handler.Crashlytics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.spongepowered.asm.mixin.Mixins;

import java.io.IOException;
import java.util.*;

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

    private static void runInit(String init, Module<?> module) {
        run(() -> {
            Bus<InitEvent> event = module.getOrCreateBus(init + "_init_event", null);
            if (event == null) return;
            event.invoker().collect().forEach(module::initClass);
        }, (b) -> b.literal("Failed to execute %s!".formatted(init)).add("module", module.meta().id()));
    }

    @Environment(EnvType.CLIENT)
    public static void onClient() {
        Status.update();

        onMerged();

        for (Module<?> module : ModuleManager.get().loaded()) {
            if (module.meta().environment().isServer()) continue;
            runInit("client", module);
        }
        run(AndromedaClient::init, b -> b.literal("Failed to initialize AndromedaClient!"));
    }

    @Environment(EnvType.SERVER)
    public static void onServer() {
        Status.update();

        onMerged();

        for (Module<?> module : ModuleManager.get().loaded()) {
            if (module.meta().environment().isClient()) continue;
            runInit("server", module);
        }
    }

    private static void onMerged() {
        for (Module<?> module : ModuleManager.get().loaded()) {
            runInit("merged", module);
        }
    }

    public static void onMain() {
        Status.update();
        if (Mixins.getUnvisitedCount() > 0) {
            for (org.spongepowered.asm.mixin.transformer.Config config : Mixins.getConfigs()) {
                if (!config.isVisited() && config.getName().startsWith("andromeda_dynamic$$")) {
                    boolean quilt = CommonValues.platform() == CommonValues.Platform.QUILT;

                    var builder = AndromedaException.builder().report(!quilt)
                            .literal("Mixin failed to consume Andromeda's late configs!").translatable(MixinProcessor.NOTICE)
                            .add("mixin_config", config.getName());

                    if (!quilt) {
                        List<String> list = FabricLoader.getInstance().getEntrypointContainers("preLaunch", PreLaunchEntrypoint.class).stream()
                                .map(EntrypointContainer::getEntrypoint).map(Object::getClass).map(Class::getName)
                                .toList();
                        if (!list.isEmpty()) builder.add("before_andromeda", list);
                    }
                    throw builder.build();
                }
            }
        }

        for (Module<?> module : ModuleManager.get().loaded()) {
            runInit("main", module);
        }

        run(Andromeda::init, b -> b.literal("Failed to initialize Andromeda!"));
    }

    public static void onPreLaunch() {
        try {
            EarlyLanguage.load();

            Status.update();
            LOGGER.info(EarlyLanguage.translate("andromeda.bootstrap.loading", CommonValues.version(), CommonValues.platform(), CommonValues.platform().version()));

            AndromedaConfig.save();
            Experiments.save();

            Status.update();

            List<Module.Zygote> list = new ArrayList<>(40);
            run(() -> {
                //This should probably be removed.
                ServiceLoader.load(Module.class).stream().map(p -> Module.Zygote.spawn(p.type(), p::get)).forEach(list::add);
                EntrypointRunner.run("andromeda:modules", ModuleManager.ModuleSupplier.class, s -> list.addAll(s.get()));
            }, (b) -> b.literal("Failed during module discovery!"));

            if (list.isEmpty()) {
                LOGGER.error(EarlyLanguage.translate("andromeda.bootstrap.no_modules"));
            }

            list.removeIf(m -> CommonValues.environment() == EnvType.SERVER && !m.meta().environment().allows(EnvType.SERVER));

            resolveConflicts(list);

            List<Module.Zygote> sorted = list.stream().sorted(Comparator.comparingInt(m -> {
                int i = ModuleManager.CATEGORIES.indexOf(m.meta().category());
                return i >= 0 ? i : ModuleManager.CATEGORIES.size();
            })).toList();

            Status.update();

            ModuleManager m;
            try {
                ModuleManager.INSTANCE = (m = new ModuleManager(sorted));
            } catch (Throwable t) {//Manager constructor does a lot of heavy-lifting, so we want to catch any errors.
                throw AndromedaException.builder()
                        .cause(t).literal("Failed to initialize ModuleManager!!!")
                        .build();
            }
            m.print();
            //Scan for mixins.
            m.loaded().forEach(module -> getModuleClassPath().addUrl(module.getClass().getProtectionDomain().getCodeSource().getLocation()));
            run(() -> m.getMixinProcessor().addMixins(), (b) -> b.literal("Failed to inject dynamic mixin configs!").translatable(MixinProcessor.NOTICE));
            Support.share("andromeda:module_manager", m);

            Status.update();
            Crashlytics.addHandler("andromeda", CrashHandler::handleCrash);
        } catch (Throwable t) {
            var e = AndromedaException.builder().cause(t).literal("Failed to bootstrap Andromeda!").build();
            CrashHandler.handleCrash(e, Context.of());
            e.setAppender(b -> b.append("Statuses: ").append(AndromedaException.GSON.toJson(e.getStatuses())));
            throw e;
        }
    }

    private static void resolveConflicts(Collection<Module.Zygote> list) {
        Map<String, Module.Zygote> packages = new HashMap<>();
        Map<String, Module.Zygote> ids = new HashMap<>();
        for (Module.Zygote module : list) {
            ModuleManager.validateZygote(module);

            var id = ids.put(module.meta().id(), module);
            if (id != null)
                throw AndromedaException.builder()
                        .literal("Duplicate module IDs!")
                        .add("identifier", module.meta().id()).add("module", id.type()).add("duplicate", module.type())
                        .build();

            var pkg = packages.put(module.type().getPackageName(), module);
            if (pkg != null)
                throw AndromedaException.builder()
                        .literal("Duplicate module packages!")
                        .add("package", module.type().getPackageName()).add("module", pkg.type()).add("duplicate", module.type())
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
        return AndromedaMixins.CLASS_PATH;
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

    public static boolean isModLoaded(Module<?> module, String mod) {
        return !Debug.skipIntegration(module.meta().id(), mod) && FabricLoader.getInstance().isModLoaded(mod);
    }

    public enum Status {
        PRE_INIT, INIT, DISCOVERY, SETUP,
        PRE_LAUNCH, MAIN, DEFAULT;

        private static final Map<Status, Status> PROGRESS = ImmutableMap.<Status, Status>builder()
                .put(PRE_INIT, INIT)
                .put(INIT, DISCOVERY)
                .put(DISCOVERY, SETUP)
                .put(SETUP, PRE_LAUNCH)
                .put(PRE_LAUNCH, MAIN)
                .put(MAIN, DEFAULT)
                .build();
        private static Status CURRENT = PRE_INIT;

        public static void update() {
            var progress = PROGRESS.get(Status.CURRENT);
            if (progress == null) throw new IllegalStateException();
            Status.CURRENT = progress;
            LOGGER.debug("Status updated to {}", Status.CURRENT);
        }

        public static synchronized Status get() {
            return CURRENT;
        }
    }
}

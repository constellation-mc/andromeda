package me.melontini.andromeda.base;

import com.google.gson.JsonObject;
import lombok.CustomLog;
import me.melontini.andromeda.base.events.Bus;
import me.melontini.andromeda.base.events.MixinConfigEvent;
import me.melontini.andromeda.base.util.ModulePlugin;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.andromeda.util.mixin.AndromedaMixins;
import me.melontini.dark_matter.api.base.reflect.wrappers.GenericField;
import me.melontini.dark_matter.api.base.reflect.wrappers.GenericMethod;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.FabricUtil;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The MixinProcessor is responsible for injecting dynamic mixin configs.
 * <p> This is done by creating a config with GSON, passing it to the {@link ByteArrayInputStream}, and injecting this input stream to our temporary fake mixin service using a {@link ThreadLocal}.
 * <p> This must be done during {@code 'preLaunch'} as no classes should be transformed at this point.
 */
@CustomLog
public class MixinProcessor {

    public static final String NOTICE = "## Mixin configs are internal mod components and are not the same as user configs! ##";
    public static final String JAVA_VERSION = "JAVA_17";
    public static final String MIXIN_VERSION = "0.8.5";

    private static final ThreadLocal<InputStream> CONFIG = ThreadLocal.withInitial(() -> null);
    private boolean done = false;
    private final ModuleManager manager;
    private final Map<String, Module<?>> mixinConfigs = new HashMap<>();
    private final Map<String, List<String>> mixinClasses = new ConcurrentHashMap<>();

    public MixinProcessor(ModuleManager manager) {
        this.manager = manager;
    }

    @ApiStatus.Internal
    public Optional<Module<?>> fromConfig(String name) {
        return Optional.ofNullable(mixinConfigs.get(name));
    }

    @ApiStatus.Internal
    public List<String> mixinsFromPackage(String pkg) {
        return mixinClasses.get(pkg);
    }

    public void addMixins() {
        if (done) return;

        CompletableFuture.allOf(manager.loaded().stream().map(module -> CompletableFuture.runAsync(() -> {
            String pkg = module.getClass().getPackageName() + ".mixin";
            var list = AndromedaMixins.discoverInPackage(pkg);
            if (!list.isEmpty()) mixinClasses.put(pkg, list);
        })).toArray(CompletableFuture[]::new)).join();

        IMixinService service = MixinService.getService();
        this.injectService(service);

        this.manager.loaded().stream().filter(module -> mixinClasses.containsKey(module.getClass().getPackageName() + ".mixin")).forEach((module) -> {
            JsonObject config = createConfig(module);

            String cfg = "andromeda_dynamic$$" + module.meta().dotted() + ".mixins.json";
            try (ByteArrayInputStream bais = new ByteArrayInputStream(config.toString().getBytes())) {
                CONFIG.set(bais);//Is there a safer way to do this?
                Mixins.addConfiguration(cfg);
                this.mixinConfigs.put(cfg, module);
            } catch (IOException e) {
                throw AndromedaException.builder()
                        .message("Couldn't inject mixin config for module '%s'".formatted(module.meta().id())).message(NOTICE)
                        .add("mixin_config", cfg).add("module", module.meta().id()).build();
            } finally {
                CONFIG.remove();
            }
        });
        this.dejectService(service);
        done = true;

        Mixins.getConfigs().forEach(config -> {
            if (this.mixinConfigs.containsKey(config.getName())) {
                config.getConfig().decorate(FabricUtil.KEY_MOD_ID, CommonValues.MODID);
            }
        });
    }

    public JsonObject createConfig(Module<?> module) {
        JsonObject object = new JsonObject();
        object.addProperty("required", true);
        object.addProperty("minVersion", MIXIN_VERSION);
        object.addProperty("package", module.getClass().getPackageName() + ".mixin");
        object.addProperty("compatibilityLevel", JAVA_VERSION);
        object.addProperty("plugin", ModulePlugin.class.getName());
        object.addProperty("refmap", "andromeda-refmap.json");
        JsonObject injectors = new JsonObject();
        injectors.addProperty("defaultRequire", 1);
        injectors.addProperty("maxShiftBy", 3);
        object.add("injectors", injectors);

        Bus<MixinConfigEvent> bus = module.getOrCreateBus("mixin_config_event", null);
        if (bus != null) bus.invoker().accept(object);

        return object;
    }

    private final GenericMethod<?, MixinService> GET_INSTANCE = GenericMethod.of(MixinService.class, "getInstance");
    private final GenericField<MixinService, IMixinService> SERVICE = GenericField.of(MixinService.class, "service");

    public void injectService(IMixinService currentService) {
        IMixinService service = (IMixinService) Proxy.newProxyInstance(MixinProcessor.class.getClassLoader(), new Class[]{IMixinService.class}, (proxy, method, args) -> {
            if (method.getName().equals("getResourceAsStream")) {
                if (args[0] instanceof String s) {
                    if (s.startsWith("andromeda_dynamic$$")) {
                        return CONFIG.get();
                    }
                }
            }

            return method.invoke(currentService, args);
        });
        MixinService serviceProxy = GET_INSTANCE.accessible(true).invoke(null);
        SERVICE.accessible(true).set(serviceProxy, service);
    }

    public void dejectService(IMixinService realService) {
        MixinService serviceProxy = GET_INSTANCE.accessible(true).invoke(null);
        SERVICE.accessible(true).set(serviceProxy, realService);
    }
}

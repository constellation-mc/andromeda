package me.melontini.andromeda.base;

import com.google.gson.JsonObject;
import lombok.CustomLog;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.andromeda.util.mixin.AndromedaMixins;
import me.melontini.dark_matter.api.base.reflect.wrappers.GenericField;
import me.melontini.dark_matter.api.base.reflect.wrappers.GenericMethod;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.base.util.mixin.IPluginPlugin;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.FabricUtil;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

@CustomLog
public class MixinProcessor {

    private static final ThreadLocal<InputStream> CONFIG = ThreadLocal.withInitial(() -> null);
    private static boolean done = false;

    public static void addMixins(ModuleManager manager) {
        if (done) return;
        IMixinService service = MixinService.getService();
        MixinProcessor.injectService(service);
        manager.loaded().forEach((module) -> {
            JsonObject config = createConfig(module);

            String cfg = "andromeda_dynamic$$" + module.meta().dotted() + ".mixins.json";
            try (ByteArrayInputStream bais = new ByteArrayInputStream(config.toString().getBytes())) {
                CONFIG.set(bais);
                Mixins.addConfiguration(cfg);
                manager.mixinConfigs.put(cfg, module);
            } catch (IOException e) {
                throw new AndromedaException.Builder()
                        .message("Couldn't inject mixin config for module '%s'".formatted(module.meta().id()))
                        .add("mixin_config", cfg).add("module", module.meta().id()).build();
            } finally {
                CONFIG.remove();
            }
        });
        MixinProcessor.dejectService(service);
        done = true;

        Mixins.getConfigs().forEach(config1 -> {
            if (manager.mixinConfigs.containsKey(config1.getName())) {
                config1.getConfig().decorate(FabricUtil.KEY_MOD_ID, "andromeda");
            }
        });
    }

    public static JsonObject createConfig(Module<?> module) {
        JsonObject object = new JsonObject();
        object.addProperty("required", true);
        object.addProperty("minVersion", "0.8");
        object.addProperty("package", module.mixins());
        object.addProperty("compatibilityLevel", "JAVA_17");
        object.addProperty("plugin", Plugin.class.getName());
        object.addProperty("refmap", "andromeda-refmap.json");
        JsonObject injectors = new JsonObject();
        injectors.addProperty("defaultRequire", 1);
        object.add("injectors", injectors);

        module.acceptMixinConfig(object);

        return object;
    }

    private static final GenericMethod<?, MixinService> GET_INSTANCE = GenericMethod.of(MixinService.class, "getInstance");
    private static final GenericField<MixinService, IMixinService> SERVICE = GenericField.of(MixinService.class, "service");

    public static void injectService(IMixinService currentService) {
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

    public static void dejectService(IMixinService realService) {
        MixinService serviceProxy = GET_INSTANCE.accessible(true).invoke(null);
        SERVICE.accessible(true).set(serviceProxy, realService);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class Plugin extends ExtendablePlugin {

        private static final String MIXIN_ENVIRONMENT_ANNOTATION = "L" + SpecialEnvironment.class.getName().replace(".", "/") + ";";

        private String mixinPackage;

        @Override
        protected void collectPlugins(Set<IPluginPlugin> plugins) {
            plugins.add(DefaultPlugins.constructDummyPlugin());
        }

        protected void onPluginLoad(String mixinPackage) {
            this.mixinPackage = mixinPackage;
        }

        protected void getMixins(List<String> mixins) {
            mixins.addAll(AndromedaMixins.discoverInPackage(this.mixinPackage));
        }

        @Override
        protected void afterApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
            if (targetClass.visibleAnnotations != null && !targetClass.visibleAnnotations.isEmpty()) {//strip our annotation from the class
                targetClass.visibleAnnotations.removeIf(node -> MIXIN_ENVIRONMENT_ANNOTATION.equals(node.desc));
            }
        }
    }
}

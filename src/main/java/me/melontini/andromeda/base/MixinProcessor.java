package me.melontini.andromeda.base;

import com.google.common.reflect.ClassPath;
import lombok.CustomLog;
import me.melontini.andromeda.base.annotations.MixinEnvironment;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.exceptions.MixinVerifyError;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.base.util.mixin.AsmUtil;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.base.util.mixin.IPluginPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

@CustomLog
public class MixinProcessor {

    public static boolean checkNode(ClassNode n) {
        boolean load = true;
        AnnotationNode envNode = Annotations.getVisible(n, MixinEnvironment.class);
        if (envNode != null) {
            EnvType value = AsmUtil.getAnnotationValue(envNode, "value", null);
            if (value != null) {
                if (value != CommonValues.environment()) return false;
            }
        }

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) verifyMixin(n, n.name);

        return load;
    }

    private static void verifyMixin(ClassNode mixinNode, String mixinClassName) {
        LOGGER.debug("Verifying access flags!");
        if ((mixinNode.access & Modifier.PUBLIC) == Modifier.PUBLIC) {
            throw new MixinVerifyError("Public Mixin! " + mixinClassName);
        }
    }

    public static void injectService(IMixinService currentService) {
        IMixinService service = (IMixinService) Proxy.newProxyInstance(MixinProcessor.class.getClassLoader(), new Class[]{IMixinService.class}, (proxy, method, args) -> {
            if (method.getName().equals("getResourceAsStream")) {
                if (args[0] instanceof String s) {
                    if (s.startsWith("andromeda$$")) {
                        return ModuleManager.CONFIG.get();
                    }
                }
            }

            return method.invoke(currentService, args);
        });
        Utilities.runUnchecked(() -> {
            Method m = MixinService.class.getDeclaredMethod("getInstance");
            m.setAccessible(true);
            MixinService serviceProxy = (MixinService) m.invoke(null);

            Field f = MixinService.class.getDeclaredField("service");
            f.setAccessible(true);
            f.set(serviceProxy, service);
        });
    }

    public static void dejectService(IMixinService realService) {
        Utilities.runUnchecked(() -> {
            Method m = MixinService.class.getDeclaredMethod("getInstance");
            m.setAccessible(true);
            MixinService serviceProxy = (MixinService) m.invoke(null);

            Field f = MixinService.class.getDeclaredField("service");
            f.setAccessible(true);
            f.set(serviceProxy, realService);
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class Plugin extends ExtendablePlugin {
        private String mixinPackage;

        @Override
        protected void collectPlugins(Set<IPluginPlugin> plugins) {
            plugins.add(DefaultPlugins.constructDummyPlugin());
        }

        protected void onPluginLoad(String mixinPackage) {
            this.mixinPackage = mixinPackage;
        }

        protected void getMixins(List<String> mixins) {
            ClassPath p = Utilities.supplyUnchecked(() -> ClassPath.from(Plugin.class.getClassLoader()));

            p.getTopLevelClassesRecursive(this.mixinPackage).stream()
                    .map(ClassPath.ClassInfo::getName)
                    .map((s) -> Utilities.supplyUnchecked(() -> MixinService.getService().getBytecodeProvider().getClassNode(s.replace('.', '/'))))
                    .filter(MixinProcessor::checkNode)
                    .map((n) -> n.name.replace('/', '.').substring((this.mixinPackage + ".").length()))
                    .forEach(mixins::add);
        }
    }
}

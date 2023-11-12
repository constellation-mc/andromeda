package me.melontini.andromeda.base;

import com.google.common.base.Suppliers;
import com.google.common.reflect.ClassPath;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.client.AndromedaClient;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.annotations.config.Environment;
import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.fabricmc.api.EnvType;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ModuleManager {

    private static final Supplier<ModuleManager> INSTANCE = Suppliers.memoize(ModuleManager::new);
    private static final int modulePrefixLength = "me.melontini.andromeda.modules.".length();

    private final List<Module> modules = new ArrayList<>();

    public void collect() {
        Utilities.supplyUnchecked(() -> ClassPath.from(ModuleManager.class.getClassLoader())).getTopLevelClassesRecursive("me.melontini.andromeda.modules")
                .stream().map(ClassPath.ClassInfo::getName).map(s -> Utilities.supplyUnchecked(() -> ClassInfo.forName(s)))
                .filter(ci -> ci.getInterfaces().contains(Module.class.getName().replace(".", "/")))
                .map(ci -> Utilities.supplyUnchecked(() -> Class.forName(ci.getClassName())))
                .map(cls -> Utilities.supplyUnchecked(() -> Reflect.setAccessible(cls.getDeclaredConstructor())))
                .forEach(ctx -> modules.add((Module) Utilities.supplyUnchecked(ctx::newInstance)));
        modules.removeIf(m -> !m.enabled() || (m.environment() == Environment.CLIENT && CommonValues.environment() == EnvType.SERVER));
        modules.forEach(m -> AndromedaLog.info("Loading module: {}", m.getClass().getPackageName().substring(modulePrefixLength)));
    }

    public List<String> getMixins() {
        ClassPath p = Utilities.supplyUnchecked(() -> ClassPath.from(ModuleManager.class.getClassLoader()));

        List<String> mixins = new ArrayList<>();
        modules.forEach(m -> {
            String s = "me.melontini.andromeda.mixin." + m.getClass().getPackageName().substring(modulePrefixLength);
            p.getTopLevelClassesRecursive(s).stream().map(ClassPath.ClassInfo::getName).forEach(mixins::add);
        });
        //mixins.forEach(s -> AndromedaLog.info("Discovered {}", s));
        return mixins.stream().map(s -> Utilities.supplyUnchecked(() -> MixinService.getService().getBytecodeProvider().getClassNode(s.replace('.', '/'))))
                .filter(MixinProcessor::checkNode).map(n -> n.name.replace('/', '.').substring("me.melontini.andromeda.mixin.".length())).toList();
    }

    public static ModuleManager get() {
        return INSTANCE.get();
    }

    public static void onClient() {
        get().modules.forEach(Module::onClient);
        AndromedaClient.init();
    }

    public static void onServer() {
        get().modules.forEach(Module::onServer);
    }

    public static void onMain() {
        get().modules.forEach(Module::onMain);
        Andromeda.init();
    }

    public static void onPreLaunch() {
        get().modules.forEach(Module::onPreLaunch);
    }
}

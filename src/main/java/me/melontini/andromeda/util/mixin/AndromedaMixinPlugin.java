package me.melontini.andromeda.util.mixin;

import com.google.common.reflect.ClassPath;
import lombok.CustomLog;
import me.melontini.andromeda.base.MixinProcessor;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.MixinEnvironment;
import me.melontini.andromeda.base.config.Config;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.base.util.mixin.IPluginPlugin;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
@CustomLog
public class AndromedaMixinPlugin extends ExtendablePlugin {

    private static final String MIXIN_ENVIRONMENT_ANNOTATION = "L" + MixinEnvironment.class.getName().replace(".", "/") + ";";

    static {
        try {
            FrameworkPatch.patch();
        } catch (Throwable e) {
            LOGGER.error("Failed to patch the mixin framework!", e);
        }
    }

    @Override
    protected void collectPlugins(Set<IPluginPlugin> plugins) {
        plugins.add(DefaultPlugins.constructDummyPlugin());
    }

    @Override
    public void onPluginLoad(String mixinPackage) {
        LOGGER.info("Andromeda({}) on {}({})", CommonValues.version(), CommonValues.platform(), CommonValues.platform().version());
        Mixins.registerErrorHandlerClass(ErrorHandler.class.getName());

        Path newCfg = FabricLoader.getInstance().getConfigDir().resolve("andromeda.json");
        if (Files.exists(newCfg)) {
            try {
                Files.move(newCfg, CommonValues.configPath());
            } catch (IOException e) {
                AndromedaLog.error("Couldn't rename old m-tweaks config!", e);
            }
        }
        Config.get();

        if (this.isDev()) LOGGER.warn("Will be verifying mixins!");

        ModuleManager.get().prepare();
        ModuleManager.get().print();
    }

    @Override
    protected void getMixins(List<String> mixins) {
        ClassPath p = Utilities.supplyUnchecked(() -> ClassPath.from(AndromedaMixinPlugin.class.getClassLoader()));

        ModuleManager.get().loaded().stream().filter(m->m.getClass().getName().startsWith("me.melontini.andromeda.modules"))
                .forEach(m -> p.getTopLevelClassesRecursive("me.melontini.andromeda.mixin." + m.id().replace('/', '.')).stream()
                        .map(ClassPath.ClassInfo::getName)
                        .map(s -> Utilities.supplyUnchecked(() -> MixinService.getService().getBytecodeProvider().getClassNode(s.replace('.', '/'))))
                        .filter(MixinProcessor::checkNode)
                        .map(n -> n.name.replace('/', '.').substring("me.melontini.andromeda.mixin.".length()))
                        .forEach(mixins::add));
    }

    @Override
    public void afterApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (targetClass.visibleAnnotations != null && !targetClass.visibleAnnotations.isEmpty()) {//strip our annotation from the class
            targetClass.visibleAnnotations.removeIf(node -> MIXIN_ENVIRONMENT_ANNOTATION.equals(node.desc));
        }
    }
}

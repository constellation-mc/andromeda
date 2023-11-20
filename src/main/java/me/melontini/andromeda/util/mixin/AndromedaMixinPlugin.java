package me.melontini.andromeda.util.mixin;

import lombok.CustomLog;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.MixinEnvironment;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.base.util.mixin.IPluginPlugin;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

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

        Path mtConfig = FabricLoader.getInstance().getConfigDir().resolve("m-tweaks.json");
        if (Files.exists(mtConfig)) {
            try {
                Files.move(mtConfig, CommonValues.configPath());
            } catch (IOException e) {
                AndromedaLog.error("Couldn't rename old m-tweaks config!", e);
            }
        }

        if (this.isDev()) LOGGER.warn("Will be verifying mixins!");

        ModuleManager.get().collect();
        ModuleManager.get().print();
    }

    @Override
    protected void getMixins(List<String> mixins) {
        mixins.addAll(ModuleManager.get().getMixins());
    }

    @Override
    public void afterApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (targetClass.visibleAnnotations != null && !targetClass.visibleAnnotations.isEmpty()) {//strip our annotation from the class
            targetClass.visibleAnnotations.removeIf(node -> MIXIN_ENVIRONMENT_ANNOTATION.equals(node.desc));
        }
    }
}

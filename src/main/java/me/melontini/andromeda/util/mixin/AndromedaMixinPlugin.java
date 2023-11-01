package me.melontini.andromeda.util.mixin;

import lombok.CustomLog;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.andromeda.util.exceptions.MixinVerifyError;
import me.melontini.dark_matter.api.base.util.mixin.AsmUtil;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.base.util.mixin.IPluginPlugin;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Annotations;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static me.melontini.dark_matter.api.base.util.Utilities.cast;

@SuppressWarnings("UnstableApiUsage")
@CustomLog
public class AndromedaMixinPlugin extends ExtendablePlugin {

    private static final String MIXIN_TO_OPTION_ANNOTATION = "L" + Feature.class.getName().replace(".", "/") + ";";

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
        Config.get();

        if (Config.get().compatMode) LOGGER.warn("Compat mode is on!");
        if (this.isDev()) LOGGER.warn("Will be verifying mixins!");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName, ClassNode mixinNode, List<AnnotationNode> nodes) {
        boolean load = true;
        if (Config.get().compatMode) {
            AnnotationNode annotationNode = Annotations.getVisible(mixinNode, Feature.class);
            //"inspired" by https://github.com/unascribed/Fabrication/blob/3.0/1.18/src/main/java/com/unascribed/fabrication/support/MixinConfigPlugin.java
            if (annotationNode != null) {
                Map<String, Object> values = AsmUtil.mapAnnotationNode(annotationNode);
                List<String> configOptions = cast(values.get("value"));
                for (String configOption : configOptions) {
                    try {
                        load = Config.get(configOption);
                    } catch (Exception e) {
                        LOGGER.warn("Couldn't check @MixinRelatedConfigOption(%s) from %s This is no fault of yours.".formatted(configOption, mixinClassName), e);
                    }
                    if (!load) break;
                }
            }
        }
        if (this.isDev()) verifyMixin(mixinNode, mixinClassName);

        return load;
    }

    private static void verifyMixin(ClassNode mixinNode, String mixinClassName) {
        LOGGER.debug("Verifying @MixinRelatedConfigOption from " + mixinClassName);
        AnnotationNode annotationNode = Annotations.getVisible(mixinNode, Feature.class);
        if (annotationNode != null) {
            Map<String, Object> values = AsmUtil.mapAnnotationNode(annotationNode);
            List<String> configOptions = cast(values.get("value"));
            boolean dummy = true;
            for (String configOption : configOptions) {
                try {
                    dummy = Config.get(configOption);
                } catch (NoSuchFieldException e) {
                    throw new MixinVerifyError("Invalid config option in @MixinRelatedConfigOption(%s) from %s".formatted(configOption, mixinClassName));
                } catch (ClassCastException e) {
                    throw new MixinVerifyError("Non-boolean config option in @MixinRelatedConfigOption(%s) from %s".formatted(configOption, mixinClassName));
                } catch (Exception e) {
                    throw new MixinVerifyError("Exception while evaluating shouldApplyMixin", e);
                }
            }
            LOGGER.debug("Verified @MixinRelatedConfigOption from %s. State: %s".formatted(mixinClassName, dummy));
        } else LOGGER.debug("No @MixinRelatedConfigOption found in " + mixinClassName);

        LOGGER.debug("Verifying access flags!");
        if ((mixinNode.access & Modifier.PUBLIC) == Modifier.PUBLIC) {
            throw new MixinVerifyError("Public Mixin! " + mixinClassName);
        }
    }

    @Override
    public void afterApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (targetClass.visibleAnnotations != null && !targetClass.visibleAnnotations.isEmpty()) {//strip our annotation from the class
            targetClass.visibleAnnotations.removeIf(node -> MIXIN_TO_OPTION_ANNOTATION.equals(node.desc));
        }
    }
}

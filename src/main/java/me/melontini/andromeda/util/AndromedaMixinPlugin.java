package me.melontini.andromeda.util;

import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.PrependingLogger;
import me.melontini.dark_matter.api.base.util.mixin.AsmUtil;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.base.util.mixin.IPluginPlugin;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Annotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class AndromedaMixinPlugin extends ExtendablePlugin {
    private static final PrependingLogger LOGGER = new PrependingLogger(LogManager.getLogger("AndromedaMixinPlugin"), PrependingLogger.LOGGER_NAME);
    private static final String MIXIN_TO_OPTION_ANNOTATION = "L" + MixinRelatedConfigOption.class.getName().replace(".", "/") + ";";
    private static AndromedaConfig CONFIG;

    static {
        LOGGER.info("Definitely up to a lot of good");
    }

    @Override
    protected void collectPlugins(Set<IPluginPlugin> plugins) {
        plugins.add(DefaultPlugins.constructDummyPlugin());
    }

    @Override
    public void onPluginLoad(String mixinPackage) {
        LOGGER.info("Platform: " + SharedConstants.PLATFORM);
        Path mtConfig = FabricLoader.getInstance().getConfigDir().resolve("m-tweaks.json");
        if (Files.exists(mtConfig)) {
            try {
                Files.move(mtConfig, SharedConstants.CONFIG_PATH);
            } catch (IOException e) {
                AndromedaLog.error("Couldn't rename old m-tweaks config!", e);
            }
        }
        CONFIG = ConfigHelper.loadConfigFromFile();
        boolean log = CONFIG.debugMessages || this.isDev();
        AndromedaLog.setDebug(log);

        if (CONFIG.compatMode) {
            LOGGER.warn("Compat mode is on!");
        }
        if (this.isDev()) LOGGER.warn("Will be verifying config annotations!");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName, ClassNode mixinNode, List<AnnotationNode> nodes) {
        boolean load = true;
        if (CONFIG.compatMode) {
            AnnotationNode annotationNode = Annotations.getVisible(mixinNode, MixinRelatedConfigOption.class);
            //"inspired" by https://github.com/unascribed/Fabrication/blob/3.0/1.18/src/main/java/com/unascribed/fabrication/support/MixinConfigPlugin.java
            if (annotationNode != null) {
                Map<String, Object> values = AsmUtil.mapAnnotationNode(annotationNode);
                List<String> configOptions = (List<String>) values.get("value");
                for (String configOption : configOptions) {
                    try {
                        load = (boolean) ConfigHelper.getConfigOption(configOption, CONFIG);
                    } catch (Exception e) {
                        LOGGER.warn("Couldn't check @MixinRelatedConfigOption(%s) from %s This is no fault of yours.".formatted(configOption, mixinClassName), e);
                    }
                    if (!load) break;
                }
            }
        }
        if (this.isDev()) verifyConfigAnnotation(mixinNode, mixinClassName);

        return load;
    }

    private static void verifyConfigAnnotation(ClassNode mixinNode, String mixinClassName) {
        LOGGER.debug("Verifying @MixinRelatedConfigOption from " + mixinClassName);
        AnnotationNode annotationNode = Annotations.getVisible(mixinNode, MixinRelatedConfigOption.class);
        if (annotationNode != null) {
            Map<String, Object> values = AsmUtil.mapAnnotationNode(annotationNode);
            List<String> configOptions = (List<String>) values.get("value");
            boolean dummy = true;
            for (String configOption : configOptions) {
                try {
                    dummy = (boolean) ConfigHelper.getConfigOption(configOption, CONFIG);
                } catch (NoSuchFieldException e) {
                    throw new AndromedaException("Invalid config option in @MixinRelatedConfigOption(%s) from %s".formatted(configOption, mixinClassName));
                } catch (ClassCastException e) {
                    throw new AndromedaException("Non-boolean config option in @MixinRelatedConfigOption(%s) from %s".formatted(configOption, mixinClassName));
                } catch (Exception e) {
                    throw new AndromedaException("Exception while evaluating shouldApplyMixin", e);
                }
            }
            LOGGER.debug("Verified @MixinRelatedConfigOption from %s. State: %s".formatted(mixinClassName, dummy));
        } else LOGGER.debug("No @MixinRelatedConfigOption found in " + mixinClassName);
    }

    @Override
    public void afterApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (targetClass.visibleAnnotations != null && !targetClass.visibleAnnotations.isEmpty()) {//strip our annotation from the class
            targetClass.visibleAnnotations.removeIf(node -> MIXIN_TO_OPTION_ANNOTATION.equals(node.desc));
        }
    }
}

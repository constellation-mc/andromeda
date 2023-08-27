package me.melontini.andromeda.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.config.FeatureManager;
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

public class AndromedaMixinPlugin extends ExtendablePlugin {
    private static final PrependingLogger LOGGER = new PrependingLogger(LogManager.getLogger("AndromedaMixinPlugin"), PrependingLogger.LOGGER_NAME);
    private static final String MIXIN_TO_OPTION_ANNOTATION = "L" + MixinRelatedConfigOption.class.getName().replace(".", "/") + ";";
    private static AndromedaConfig CONFIG;
    private static boolean log;

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
        loadConfigFromFile();
        log = CONFIG.debugMessages || FabricLoader.getInstance().isDevelopmentEnvironment();
        AndromedaLog.setDebug(log);

        if (CONFIG.compatMode) {
            LOGGER.warn("Compat mode is on!");
        }
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
                    } catch (NoSuchFieldException e) {
                        throw new AndromedaException("Invalid config option in MixinRelatedConfigOption: " + configOption + " This is no fault of yours.");
                    } catch (ClassCastException e) {
                        throw new AndromedaException("Non-boolean config option in MixinRelatedConfigOption: " + configOption + " This is no fault of yours.");
                    } catch (Exception e) {
                        throw new AndromedaException("Exception while evaluating shouldApplyMixin", e);
                    }
                    if (!load) break;
                }
            }
        }
        if (log)
            LOGGER.info("{} ({}) : {}", mixinClassName.replaceFirst("me\\.melontini\\.andromeda\\.mixin\\.", ""),
                    targetClassName.replaceFirst("net\\.minecraft\\.", ""), load ? "applied ✅" : "skipped ⏩");
        return load;
    }

    @Override
    public void afterApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (targetClass.visibleAnnotations != null && !targetClass.visibleAnnotations.isEmpty()) {//strip our annotation from the class
            targetClass.visibleAnnotations.removeIf(node -> MIXIN_TO_OPTION_ANNOTATION.equals(node.desc));
        }
    }

    private static void loadConfigFromFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path config = SharedConstants.CONFIG_PATH;
        if (Files.exists(config)) {
            try {
                CONFIG = gson.fromJson(Files.readString(config), AndromedaConfig.class);
                FeatureManager.processFeatures(CONFIG);
                Files.write(config, gson.toJson(CONFIG).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            CONFIG = new AndromedaConfig();
            FeatureManager.processFeatures(CONFIG);
            try {
                Files.createFile(config);
                Files.write(config, gson.toJson(CONFIG).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

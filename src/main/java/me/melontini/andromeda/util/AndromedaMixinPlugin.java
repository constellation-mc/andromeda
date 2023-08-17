package me.melontini.andromeda.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.melontini.andromeda.config.AndromedaConfig;
import me.melontini.andromeda.config.AndromedaFeatureManager;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.PrependingLogger;
import me.melontini.dark_matter.api.base.util.mixin.ExtendedPlugin;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AndromedaMixinPlugin extends ExtendedPlugin {
    private static final PrependingLogger LOGGER = new PrependingLogger(LogManager.getLogger("AndromedaMixinPlugin"), PrependingLogger.LOGGER_NAME);
    private static final String MIXIN_TO_OPTION_ANNOTATION = "L" + MixinRelatedConfigOption.class.getName().replace(".", "/") + ";";
    private static AndromedaConfig CONFIG;
    private static boolean log;

    static {
        LOGGER.info("Definitely up to a lot of good");
    }

    @Override
    public void onLoad(String mixinPackage) {
        super.onLoad(mixinPackage);
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
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean load = super.shouldApplyMixin(targetClassName, mixinClassName);
        if (CONFIG.compatMode && load) {
            try {
                //"inspired" by https://github.com/unascribed/Fabrication/blob/3.0/1.18/src/main/java/com/unascribed/fabrication/support/MixinConfigPlugin.java
                ClassNode node = MixinService.getService().getBytecodeProvider().getClassNode(mixinClassName);
                if (node.visibleAnnotations != null) {
                    for (AnnotationNode node1 : node.visibleAnnotations) {
                        if (MIXIN_TO_OPTION_ANNOTATION.equals(node1.desc)) {
                            Map<String, Object> values = mapAnnotationNode(node1);
                            List<String> configOptions = (List<String>) values.get("value");
                            for (String configOption : configOptions) {
                                String[] fields = configOption.split("\\.");

                                try {
                                    if (fields.length > 1) {//🤯🤯🤯
                                        Object obj = CONFIG.getClass().getField(fields[0]).get(CONFIG);
                                        for (int i = 1; i < (fields.length - 1); i++) {
                                            obj = obj.getClass().getField(fields[i]).get(obj);
                                        }
                                        load = obj.getClass().getField(fields[fields.length - 1]).getBoolean(obj);
                                    } else {
                                        load = CONFIG.getClass().getField(configOption).getBoolean(CONFIG);
                                    }
                                } catch (NoSuchFieldException e) {
                                    throw new AndromedaException("Invalid config option in MixinRelatedConfigOption: " + configOption + " This is no fault of yours.");
                                }
                                if (!load) break;
                            }
                        }
                    }
                }
            } catch (IllegalAccessException | IOException | ClassNotFoundException e) {
                throw new AndromedaException("Exception while evaluating shouldApplyMixin", e);
            }
        }
        if (log)
            LOGGER.info("{} : {}", mixinClassName.replaceFirst("me\\.melontini\\.andromeda\\.mixin\\.", ""), load ? "loaded" : "not loaded");
        return load;
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);
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
                AndromedaFeatureManager.processFeatures(CONFIG);
                Files.write(config, gson.toJson(CONFIG).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            CONFIG = new AndromedaConfig();
            AndromedaFeatureManager.processFeatures(CONFIG);
            try {
                Files.createFile(config);
                Files.write(config, gson.toJson(CONFIG).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

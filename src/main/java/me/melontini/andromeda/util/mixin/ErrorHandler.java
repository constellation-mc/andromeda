package me.melontini.andromeda.util.mixin;

import me.melontini.andromeda.config.FeatureManager;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.AndromedaReporter;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.api.base.util.mixin.AsmUtil;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Annotations;

import java.util.List;
import java.util.Map;

public class ErrorHandler implements IMixinErrorHandler {

    @Override
    public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin, ErrorAction action) {
        return handleMixinError("prepare", th, mixin, action);
    }

    @Override
    public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin, ErrorAction action) {
        return handleMixinError("apply", th, mixin, action);
    }

    private static ErrorAction handleMixinError(String phase, Throwable th, IMixinInfo mixin, ErrorAction action) {
        if (action == ErrorAction.ERROR) {
            ClassNode node = mixin.getClassNode(ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE);
            AnnotationNode annotationNode = Annotations.getVisible(node, MixinRelatedConfigOption.class);
            if (annotationNode != null) {
                Map<String, Object> values = AsmUtil.mapAnnotationNode(annotationNode);
                List<String> configOptions = (List<String>) values.get("value");
                AndromedaLog.warn("Mixin({}) failed during {}. Disabling option: {}", mixin.getClassName(), phase, configOptions.get(configOptions.size() - 1));
                FeatureManager.processMixinError(configOptions.get(configOptions.size() - 1)); //We assume that the last option is the only relevant one.
                AndromedaReporter.handleCrash(th, "Mixin failed during " + phase, FabricLoader.getInstance().getEnvironmentType());
                return ErrorAction.WARN;
            }
        }
        return action;
    }
}

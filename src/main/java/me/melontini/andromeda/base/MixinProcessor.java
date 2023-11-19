package me.melontini.andromeda.base;

import lombok.CustomLog;
import me.melontini.andromeda.base.annotations.MixinEnvironment;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.andromeda.util.exceptions.MixinVerifyError;
import me.melontini.dark_matter.api.base.util.mixin.AsmUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.Annotations;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static me.melontini.dark_matter.api.base.util.Utilities.cast;

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

        /*if (Config.get().compatMode) {
            AnnotationNode annotationNode = Annotations.getVisible(n, Feature.class);
            if (annotationNode != null) {
                Map<String, Object> values = AsmUtil.mapAnnotationNode(annotationNode);
                List<String> configOptions = cast(values.get("value"));
                for (String configOption : configOptions) {
                    try {
                        load = Config.get(configOption);
                    } catch (Exception e) {
                        LOGGER.warn("Couldn't check @MixinRelatedConfigOption(%s) from %s This is no fault of yours.".formatted(configOption, n.name), e);
                    }
                    if (!load) break;
                }
            }
        }*/

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) verifyMixin(n, n.name);

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
}

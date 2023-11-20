package me.melontini.andromeda.base;

import lombok.CustomLog;
import me.melontini.andromeda.base.annotations.MixinEnvironment;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.exceptions.MixinVerifyError;
import me.melontini.dark_matter.api.base.util.mixin.AsmUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.Annotations;

import java.lang.reflect.Modifier;

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
}

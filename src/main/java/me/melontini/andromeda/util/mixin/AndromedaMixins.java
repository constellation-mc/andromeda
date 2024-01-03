package me.melontini.andromeda.util.mixin;

import lombok.CustomLog;
import me.melontini.andromeda.base.Bootstrap;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.melontini.andromeda.util.ClassPath;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.andromeda.util.exceptions.MixinVerifyError;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.base.util.mixin.AsmUtil;
import net.fabricmc.api.EnvType;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.Annotations;

import java.lang.reflect.Modifier;
import java.util.List;

@CustomLog
public class AndromedaMixins {

    private static final ClassPath CLASS_PATH = Exceptions.supply(ClassPath::from);

    public static ClassPath getClassPath() {
        return CLASS_PATH;
    }

    public static List<String> discoverInPackage(String pck) {
        return Bootstrap.getModuleClassPath().getTopLevelRecursive(pck).stream()
                .map(info -> {
                    ClassReader reader = new ClassReader(Exceptions.supply(info::readAllBytes));
                    ClassNode node = new ClassNode();
                    reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    return node;
                })
                .filter(AndromedaMixins::checkNode)
                .map((n) -> n.name.replace('/', '.').substring((pck + ".").length()))
                .toList();
    }

    public static boolean checkNode(ClassNode n) {
        if (Debug.hasKey(Debug.Keys.VERIFY_MIXINS)) verifyMixin(n, n.name);

        boolean load = true;
        AnnotationNode envNode = Annotations.getVisible(n, SpecialEnvironment.class);
        if (envNode != null) {
            Environment value = AsmUtil.getAnnotationValue(envNode, "value", Environment.BOTH);
            if (value != null) {
                return switch (value) {
                    case SERVER -> CommonValues.environment().equals(EnvType.SERVER);
                    case CLIENT -> CommonValues.environment().equals(EnvType.CLIENT);
                    case ANY -> true;
                    default -> throw new IllegalStateException(value.toString());
                };
            }
        }

        return load;
    }

    private static void verifyMixin(ClassNode mixinNode, String mixinClassName) {
        var builder = new MixinVerifyError.Builder(mixinClassName);

        if ((mixinNode.access & Modifier.PUBLIC) == Modifier.PUBLIC) {
            builder.complaint("Invalid class modifier '%s'! remove 'public'".formatted(Modifier.toString(mixinNode.access & ~Modifier.SYNCHRONIZED)));
        }
        if ((mixinNode.access & Modifier.ABSTRACT) != Modifier.ABSTRACT) {
            builder.complaint("Invalid class modifier '%s'! add 'abstract'".formatted(Modifier.toString(mixinNode.access & ~Modifier.SYNCHRONIZED)));
        }

        if (!builder.isEmpty()) throw builder.build();
        LOGGER.debug("Mixin {} passed verification!", mixinClassName);
    }
}

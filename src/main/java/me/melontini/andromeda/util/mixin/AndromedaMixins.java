package me.melontini.andromeda.util.mixin;

import com.google.common.collect.ImmutableMap;
import lombok.CustomLog;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.util.ClassPath;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.andromeda.util.exceptions.MixinVerifyError;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.base.util.mixin.AsmUtil;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.base.util.mixin.IPluginPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.Annotations;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@CustomLog
public class AndromedaMixins {

    private static final ClassPath CLASS_PATH = Exceptions.supply(ClassPath::from);

    private static final IPluginPlugin MIXIN_PREDICATE = ExtendablePlugin.DefaultPlugins.mixinPredicatePlugin();
    private static final Map<String, Predicate<ClassNode>> SPECIAL_PREDICATES = ImmutableMap.<String, Predicate<ClassNode>>builder()
            .put("common.mixin.util.DebugTrackerMixin", node -> Debug.Keys.DISPLAY_TRACKED_VALUES.isPresent())
            .build();

    public static ClassPath getClassPath() {
        return CLASS_PATH;
    }

    public static List<String> discoverInPackage(String pck) {
        return CLASS_PATH.getTopLevelRecursive(pck).stream()
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
        if (Debug.Keys.VERIFY_MIXINS.isPresent()) verifyMixin(n, n.name);

        AnnotationNode envNode = Annotations.getVisible(n, SpecialEnvironment.class);
        if (envNode != null) {
            Environment value = AsmUtil.getAnnotationValue(envNode, "value", Environment.ANY);
            if (!value.allows(CommonValues.environment())) return false;
        }

        //TODO add to Dark Matter
        var predicate = SPECIAL_PREDICATES.get(n.name.replace("/", ".").substring("me.melontini.andromeda.".length()));
        if (predicate != null && !predicate.test(n)) return false;

        //MixinPredicate only uses the node.
        return MIXIN_PREDICATE.shouldApplyMixin(null, null, n, null);
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

package me.melontini.andromeda.modules;

import com.google.common.reflect.ClassPath;
import me.melontini.andromeda.base.Bootstrap;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.Utilities;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.Annotations;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ModuleDiscovery implements ModuleManager.ModuleSupplier {
    @Override
    public List<? extends Module<?>> get() {
        return Bootstrap.getKnotClassPath().getTopLevelClassesRecursive("me.melontini.andromeda.modules")
                .stream().filter(ci -> !ci.getPackageName().endsWith("mixin") && !ci.getPackageName().endsWith("client"))
                .map(ClassPath.ResourceInfo::asByteSource).map(bs -> Utilities.supplyUnchecked(bs::read))
                .map(bytes -> {
                    ClassReader reader = new ClassReader(bytes);
                    ClassNode node = new ClassNode();
                    reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    return node;
                }).filter(node -> Annotations.getVisible(node, ModuleInfo.class) != null)
                .map(node -> Utilities.supplyUnchecked(() -> Class.forName(node.name.replace('/', '.'))))
                .map(c -> Utilities.supplyUnchecked(() -> (Module<?>) Reflect.setAccessible(Reflect.findConstructor(c).orElseThrow(() -> new IllegalStateException("Module has no no-args ctx!")))
                        .newInstance())).toList();
    }
}

package me.melontini.andromeda.modules;

import me.melontini.andromeda.base.Bootstrap;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.Utilities;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.Annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ModuleDiscovery implements ModuleManager.ModuleSupplier {
    @Override
    public List<? extends Module<?>> get() {
        Bootstrap.getModuleClassPath().addUrl(ModuleDiscovery.class.getProtectionDomain().getCodeSource().getLocation());

        List<CompletableFuture<? extends Module<?>>> futures = new ArrayList<>();
        Bootstrap.getModuleClassPath().getTopLevelRecursive("me.melontini.andromeda.modules")
                .stream().filter(ci -> !ci.packageName().endsWith("mixin") && !ci.packageName().endsWith("client"))
                .forEach(info -> futures.add(CompletableFuture.supplyAsync(() -> {
                    byte[] bytes = Utilities.supplyUnchecked(info::readAllBytes);

                    ClassReader reader = new ClassReader(bytes);
                    ClassNode node = new ClassNode();
                    reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                    if (Annotations.getVisible(node, ModuleInfo.class) != null) {
                        var c = Utilities.supplyUnchecked(() -> Class.forName(node.name.replace('/', '.')));
                        return Utilities.supplyUnchecked(() -> (Module<?>) Reflect.setAccessible(Reflect.findConstructor(c).orElseThrow(() -> new IllegalStateException("Module has no no-args ctx!")))
                                .newInstance());
                    }
                    return null;
                }, Bootstrap.getPreLaunchService())));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .handle((unused, throwable) -> futures).join().stream()
                .map(CompletableFuture::join).filter(Objects::nonNull).toList();
    }
}

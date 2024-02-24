package me.melontini.andromeda.modules;

import lombok.CustomLog;
import me.melontini.andromeda.base.Bootstrap;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.Exceptions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.Annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@CustomLog
public class ModuleDiscovery implements ModuleManager.ModuleSupplier {
    @Override
    public List<Module.Zygote> get() {
        Bootstrap.getModuleClassPath().addUrl(ModuleDiscovery.class.getProtectionDomain().getCodeSource().getLocation());

        List<CompletableFuture<String>> futures = new ArrayList<>();
        Bootstrap.getModuleClassPath().getTopLevelRecursive("me.melontini.andromeda.modules")
                .stream().filter(ci -> !ci.packageName().endsWith("mixin") && !ci.packageName().endsWith("client"))
                .forEach(info -> futures.add(CompletableFuture.supplyAsync(() -> {
                    byte[] bytes = Exceptions.supply(info::readAllBytes);

                    ClassReader reader = new ClassReader(bytes);
                    ClassNode node = new ClassNode();
                    reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                    if (Annotations.getVisible(node, ModuleInfo.class) != null) {
                        return node.name.replace('/', '.');
                    }
                    return null;
                }, ForkJoinPool.commonPool())));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .handle((unused, throwable) -> futures).join().stream()
                .map(CompletableFuture::join).filter(Objects::nonNull)
                .map(name -> {
                    var c = Exceptions.supply(() -> Class.forName(name.replace('/', '.')));
                    return Module.Zygote.spawn(c, () -> Exceptions.supply(() -> (Module<?>) Reflect.setAccessible(Reflect.findConstructor(c).orElseThrow(() -> new IllegalStateException("Module has no no-args ctx!")))
                            .newInstance()));
                }).toList();
    }
}

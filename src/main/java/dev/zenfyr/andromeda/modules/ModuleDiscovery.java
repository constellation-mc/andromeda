package dev.zenfyr.andromeda.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.util.mixin.AndromedaMixinPlugin;
import dev.zenfyr.andromeda.util.ClassPath;
import me.melontini.dark_matter.api.base.util.Exceptions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.Annotations;

public class ModuleDiscovery {

  public static final String PACKAGE = "dev.zenfyr.andromeda.modules";

  public List<Class<? extends Module>> discoverModules() {
    List<CompletableFuture<Class<? extends Module>>> futures = new ArrayList<>();

    // We scanned the .jar during Mixin plugin init.
    for (ClassPath.Info ci : AndromedaMixinPlugin.CLASS_PATH.getTopLevelRecursive(PACKAGE)) {
      if (ci.packageName().endsWith("mixin") || ci.packageName().endsWith("client")) continue;

      futures.add(CompletableFuture.supplyAsync(() -> {
            byte[] bytes = Exceptions.supply(ci::readAllBytes);

            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode();
            reader.accept(
                node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

            if (Annotations.getVisible(node, ModuleInfo.class) != null) {
              return node.name.replace('/', '.');
            }
            return null;
          })
          .thenApplyAsync(name -> {
            if (name == null) return null;
            return Exceptions.supply(
                () -> (Class<? extends Module>) Class.forName(name.replace('/', '.')));
          }));
    }
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
        .handle((unused, throwable) -> futures)
        .join()
        .stream()
        .map(CompletableFuture::join)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableList());
  }
}

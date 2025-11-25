package dev.zenfyr.andromeda.bootstrap.util.mixin;

import static dev.zenfyr.andromeda.util.AndromedaConstants.MODID;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.List;
import dev.zenfyr.andromeda.util.ClassPath;
import dev.zenfyr.andromeda.util.Util;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.mixin.AsmUtil;
import me.melontini.dark_matter.api.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.mixin.IPluginPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.Annotations;

public class AndromedaMixinPlugin extends ExtendablePlugin {

  public static final ClassPath CLASS_PATH = new ClassPath();
  private static final IPluginPlugin MIXIN_PREDICATE =
      ExtendablePlugin.DefaultPlugins.mixinPredicatePlugin();

  private String mixinPackage;

  @Override
  public void onPluginLoad(String mixinPackage) {
    this.mixinPackage = mixinPackage;

    CLASS_PATH.addPaths(
        FabricLoader.getInstance().getModContainer(MODID).orElseThrow().getRootPaths());

    if (!Files.exists(Util.HIDDEN_PATH)) {
      try {
        Files.createDirectories(Util.HIDDEN_PATH);
        if (Util.HIDDEN_PATH.getFileSystem().supportedFileAttributeViews().contains("dos"))
          Files.setAttribute(
              Util.HIDDEN_PATH, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  protected void getMixins(List<String> mixins) {
    mixins.addAll(discoverInPackage(this.mixinPackage));
  }

  public static List<String> discoverInPackage(String pck) {
    return CLASS_PATH.getTopLevelRecursive(pck).stream()
        .map(info -> {
          ClassReader reader = new ClassReader(Exceptions.supply(info::readAllBytes));
          ClassNode node = new ClassNode();
          reader.accept(
              node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
          return node;
        })
        .filter(AndromedaMixinPlugin::checkNode)
        .map((n) -> n.name.replace('/', '.').substring((pck + ".").length()))
        .collect(ImmutableList.toImmutableList());
  }

  public static boolean checkNode(ClassNode n) {
    // if (Debug.Keys.VERIFY_MIXINS.isPresent()) verifyMixin(n, n.name);

    // Validate that the mixin is loaded in a correct environment.
    var current = FabricLoader.getInstance().getEnvironmentType();
    AnnotationNode envNode = Annotations.getVisible(n, MixinEnvironment.class);
    if (envNode != null) {
      EnvType value = AsmUtil.getAnnotationValue(envNode, "value", null);
      if (current != value) return false;
    }

    // MixinPredicate only uses the node.
    return MIXIN_PREDICATE.shouldApplyMixin(null, null, n);
  }
}

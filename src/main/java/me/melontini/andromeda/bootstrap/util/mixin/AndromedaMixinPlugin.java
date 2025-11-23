package me.melontini.andromeda.bootstrap.util.mixin;

import static me.melontini.andromeda.util.AndromedaConstants.MODID;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.List;
import me.melontini.andromeda.util.ClassPath;
import me.melontini.andromeda.util.Util;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.mixin.IPluginPlugin;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

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
    // MixinPredicate only uses the node.
    return MIXIN_PREDICATE.shouldApplyMixin(null, null, n);
  }
}
